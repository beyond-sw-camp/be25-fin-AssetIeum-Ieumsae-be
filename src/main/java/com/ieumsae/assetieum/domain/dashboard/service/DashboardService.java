package com.ieumsae.assetieum.domain.dashboard.service;

import com.ieumsae.assetieum.domain.budget.budget.entity.Budget;
import com.ieumsae.assetieum.domain.budget.history.entity.BudgetHistory;
import com.ieumsae.assetieum.domain.budget.history.type.BudgetHistoryType;
import com.ieumsae.assetieum.domain.dashboard.dto.AssetDemandResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetLedgerResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetLedgerSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetOverviewResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.EmployeeDepartmentBudgetResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetDetailResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetDetailSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.LifecycleEventResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetDetailResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetDetailSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetDetailStatus;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.RentalAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.TicketProgressSummaryResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.ticket.assetrequest.type.AssetRequestTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.rental.type.RentalTicketStatus;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

	private static final int EXPIRING_DAYS = 30;
	private static final String TANGIBLE = "TANGIBLE";
	private static final String INTANGIBLE = "INTANGIBLE";

	private final MemberRepository memberRepository;
	private final EntityManager entityManager;

	public TicketProgressSummaryResponse getTicketProgressSummary(UUID companyId, UUID departmentId) {
		return TicketProgressSummaryResponse.builder()
			.waitingReceipt(countTickets(companyId, departmentId, List.of(TicketStatus.REQUESTED)))
			.receiptCompleted(countTickets(companyId, departmentId, List.of(
				TicketStatus.DEPARTMENT_APPROVED,
				TicketStatus.ASSET_APPROVED
			)))
			.processing(countTickets(companyId, departmentId, List.of(TicketStatus.IN_PROGRESS)))
			.completed(countTickets(companyId, departmentId, List.of(TicketStatus.COMPLETED)))
			.build();
	}

	public OwnedAssetSummaryResponse getOwnedAssetSummary(UUID companyId, UUID departmentId) {
		LocalDateTime now = LocalDateTime.now();
		return OwnedAssetSummaryResponse.builder()
			.unassigned(countAvailableTangibleAssets(companyId, departmentId) + countAvailableIntangibleAssets(companyId, departmentId))
			.rentalScheduled(countTangibleAssetsByStatus(companyId, departmentId, TangibleAssetStatus.RESERVED))
			.rented(countRentedTangibleAssets(companyId, departmentId))
			.overdue(countOverdueTangibleAssets(companyId, departmentId, now))
			.build();
	}

	public ExpiringAssetSummaryResponse getExpiringAssetSummary(UUID companyId, UUID departmentId) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime limit = now.plusDays(EXPIRING_DAYS);
		return ExpiringAssetSummaryResponse.builder()
			.tangibleAssetCount(countExpiringTangibleAssets(companyId, departmentId, now, limit))
			.intangibleAssetCount(countExpiringIntangibleAssets(companyId, departmentId, now, limit))
			.build();
	}

	public TicketProgressSummaryResponse getEmployeeTicketProgressSummary(UUID companyId, UUID memberId) {
		return TicketProgressSummaryResponse.builder()
			.waitingReceipt(countMemberTickets(companyId, memberId, List.of(TicketStatus.REQUESTED)))
			.receiptCompleted(countMemberTickets(companyId, memberId, List.of(
				TicketStatus.DEPARTMENT_APPROVED,
				TicketStatus.ASSET_APPROVED
			)))
			.processing(countMemberTickets(companyId, memberId, List.of(TicketStatus.IN_PROGRESS)))
			.completed(countMemberTickets(companyId, memberId, List.of(TicketStatus.COMPLETED)))
			.build();
	}

	public RentalAssetSummaryResponse getEmployeeRentalAssetSummary(UUID companyId, UUID memberId) {
		LocalDateTime now = LocalDateTime.now();
		return RentalAssetSummaryResponse.builder()
			.rentalScheduled(countMemberRentalScheduledTickets(companyId, memberId))
			.rented(countMemberRentedTangibleAssets(companyId, memberId))
			.overdue(countMemberOverdueTangibleAssets(companyId, memberId, now))
			.build();
	}

	public OwnedAssetSummaryResponse getEmployeeOwnedAssetSummary(UUID companyId, UUID memberId) {
		LocalDateTime now = LocalDateTime.now();
		return OwnedAssetSummaryResponse.builder()
			.unassigned(0)
			.rentalScheduled(countMemberRentalScheduledTickets(companyId, memberId))
			.rented(countMemberRentedTangibleAssets(companyId, memberId))
			.overdue(countMemberOverdueTangibleAssets(companyId, memberId, now))
			.build();
	}

	public ExpiringAssetSummaryResponse getEmployeeExpiringAssetSummary(UUID companyId, UUID memberId) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime limit = now.plusDays(EXPIRING_DAYS);
		return ExpiringAssetSummaryResponse.builder()
			.tangibleAssetCount(countMemberExpiringTangibleAssets(companyId, memberId, now, limit))
			.intangibleAssetCount(countMemberExpiringIntangibleAssets(companyId, memberId, now, limit))
			.build();
	}

	public PaginationResponse<OwnedAssetDetailResponse> getOwnedAssetDetails(
		UUID companyId,
		OwnedAssetDetailSearchRequest request
	) {
		return switch (request.getStatus()) {
			case UNASSIGNED -> getUnassignedAssetDetails(companyId, request.getDepartmentId(), request.getKeyword(), request);
			case RENTAL_SCHEDULED -> getRentalScheduledAssetDetails(companyId, null, request.getDepartmentId(), request.getKeyword(), request);
			case RENTED -> getRentedAssetDetails(companyId, null, request.getDepartmentId(), request.getKeyword(), request);
			case OVERDUE -> getOverdueAssetDetails(companyId, null, request.getDepartmentId(), request.getKeyword(), request);
		};
	}

	public PaginationResponse<OwnedAssetDetailResponse> getEmployeeOwnedAssetDetails(
		UUID companyId,
		UUID memberId,
		OwnedAssetDetailSearchRequest request
	) {
		return switch (request.getStatus()) {
			case UNASSIGNED -> toPaginationResponse(List.of(), request);
			case RENTAL_SCHEDULED -> getRentalScheduledAssetDetails(companyId, memberId, null, request.getKeyword(), request);
			case RENTED -> getRentedAssetDetails(companyId, memberId, null, request.getKeyword(), request);
			case OVERDUE -> getOverdueAssetDetails(companyId, memberId, null, request.getKeyword(), request);
		};
	}

	public PaginationResponse<ExpiringAssetDetailResponse> getExpiringAssetDetails(
		UUID companyId,
		ExpiringAssetDetailSearchRequest request
	) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime limit = now.plusDays(EXPIRING_DAYS);
		if (request.getAssetType() == null) {
			return getAllExpiringAssetDetails(companyId, null, request.getDepartmentId(), request.getKeyword(), now, limit, request);
		}
		if (request.getAssetType() == AssetType.TANGIBLE) {
			return getExpiringTangibleAssetDetails(
				companyId,
				null,
				request.getDepartmentId(),
				request.getKeyword(),
				now,
				limit,
				request
			);
		}
		return getExpiringIntangibleAssetDetails(
			companyId,
			null,
			request.getDepartmentId(),
			request.getKeyword(),
			now,
			limit,
			request
		);
	}

	public PaginationResponse<ExpiringAssetDetailResponse> getEmployeeExpiringAssetDetails(
		UUID companyId,
		UUID memberId,
		ExpiringAssetDetailSearchRequest request
	) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime limit = now.plusDays(EXPIRING_DAYS);
		if (request.getAssetType() == null) {
			return getAllExpiringAssetDetails(companyId, memberId, null, request.getKeyword(), now, limit, request);
		}
		if (request.getAssetType() == AssetType.TANGIBLE) {
			return getExpiringTangibleAssetDetails(
				companyId,
				memberId,
				null,
				request.getKeyword(),
				now,
				limit,
				request
			);
		}
		return getExpiringIntangibleAssetDetails(
			companyId,
			memberId,
			null,
			request.getKeyword(),
			now,
			limit,
			request
		);
	}

	public PaginationResponse<AssetDemandResponse> getEmployeeDepartmentAssetDemands(
		UUID companyId,
		UUID memberId,
		PaginationRequest request
	) {
		UUID departmentId = getMemberDepartmentId(companyId, memberId);
		List<AssetDemandResponse> responses = new ArrayList<>();
		responses.addAll(getDepartmentTangibleAssetDemands(companyId, departmentId));
		responses.addAll(getDepartmentIntangibleAssetDemands(companyId, departmentId));
		List<AssetDemandResponse> sortedResponses = responses.stream()
			.sorted(Comparator.comparing(AssetDemandResponse::getAssetName))
			.toList();
		return toPaginationResponse(sortedResponses, request);
	}

	public EmployeeDepartmentBudgetResponse getEmployeeDepartmentBudget(UUID companyId, UUID memberId) {
		Member member = findMember(companyId, memberId);
		int year = LocalDate.now().getYear();
		List<Budget> budgets = entityManager.createQuery("""
				select b
				from Budget b
				join fetch b.department d
				where b.company.id = :companyId
					and d.id = :departmentId
					and b.budgetYear = :year
				""", Budget.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", member.getDepartment().getId())
			.setParameter("year", year)
			.getResultList();

		if (budgets.isEmpty()) {
			return EmployeeDepartmentBudgetResponse.builder()
				.departmentId(member.getDepartment().getId())
				.departmentName(member.getDepartment().getName())
				.totalAmount(BigDecimal.ZERO)
				.usedAmount(BigDecimal.ZERO)
				.remainingAmount(BigDecimal.ZERO)
				.usageRate(BigDecimal.ZERO)
				.remainingRate(BigDecimal.ZERO)
				.build();
		}

		Budget budget = budgets.get(0);
		return EmployeeDepartmentBudgetResponse.builder()
			.departmentId(member.getDepartment().getId())
			.departmentName(member.getDepartment().getName())
			.totalAmount(budget.getTotalAmount())
			.usedAmount(budget.getUsedAmount())
			.remainingAmount(budget.getAvailableAmount())
			.usageRate(percent(budget.getUsedAmount(), budget.getTotalAmount()))
			.remainingRate(percent(budget.getAvailableAmount(), budget.getTotalAmount()))
			.build();
	}

	public BudgetOverviewResponse getEmployeeBudgetOverview(
		UUID companyId,
		UUID memberId,
		PaginationRequest request
	) {
		EmployeeDepartmentBudgetResponse budget = getEmployeeDepartmentBudget(companyId, memberId);
		BudgetOverviewResponse.DepartmentBudgetSummary departmentBudget =
			BudgetOverviewResponse.DepartmentBudgetSummary.builder()
				.departmentId(budget.getDepartmentId())
				.departmentName(budget.getDepartmentName())
				.totalAmount(budget.getTotalAmount())
				.usedAmount(budget.getUsedAmount())
				.usageRate(budget.getUsageRate())
				.build();

		return BudgetOverviewResponse.builder()
			.commonBudget(null)
			.departmentBudgets(toPaginationResponse(List.of(departmentBudget), request))
			.build();
	}

	public PaginationResponse<AssetDemandResponse> getAssetDemands(UUID companyId, PaginationRequest request) {
		List<AssetDemandResponse> responses = new ArrayList<>();
		responses.addAll(getTangibleAssetDemands(companyId));
		responses.addAll(getIntangibleAssetDemands(companyId));
		List<AssetDemandResponse> sortedResponses = responses.stream()
			.sorted(Comparator.comparing(AssetDemandResponse::getAssetName))
			.toList();
		return toPaginationResponse(sortedResponses, request);
	}

	public BudgetOverviewResponse getBudgetOverview(UUID companyId, PaginationRequest request) {
		int year = LocalDate.now().getYear();
		BudgetOverviewResponse.CommonBudgetSummary commonBudget = findCommonBudget(companyId, year);
		List<BudgetOverviewResponse.DepartmentBudgetSummary> departmentBudgets = findDepartmentBudgets(companyId, year);

		return BudgetOverviewResponse.builder()
			.commonBudget(commonBudget)
			.departmentBudgets(toPaginationResponse(departmentBudgets, request))
			.build();
	}

	public PaginationResponse<LifecycleEventResponse> getLifecycleEvents(UUID companyId, PaginationRequest request) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime limit = now.plusDays(EXPIRING_DAYS);
		List<LifecycleEventResponse> events = new ArrayList<>();
		events.addAll(findReturnDueEvents(companyId, now, limit));
		events.addAll(findRentalScheduledEvents(companyId, now));
		events.addAll(findTangibleInUseEvents(companyId, now));
		events.addAll(findTangibleStatusEvents(companyId, now));
		events.addAll(findTangibleWarrantyExpiringEvents(companyId, now, limit));
		events.addAll(findIntangibleInUseEvents(companyId, now));
		events.addAll(findIntangibleExpiringEvents(companyId, now, limit));
		events.addAll(findIntangibleStatusEvents(companyId, now));

		List<LifecycleEventResponse> sortedEvents = events.stream()
			.sorted(Comparator.comparing(
				LifecycleEventResponse::getDueAt,
				Comparator.nullsLast(Comparator.naturalOrder())
			))
			.toList();
		return toPaginationResponse(sortedEvents, request);
	}

	public PaginationResponse<BudgetLedgerResponse> getBudgetLedger(
		UUID companyId,
		BudgetLedgerSearchRequest request
	) {
		String keyword = normalizeKeyword(request.getKeyword());
		List<BudgetLedgerResponse> content = entityManager.createQuery("""
				select bh
				from BudgetHistory bh
				join fetch bh.budget b
				left join fetch bh.department d
				left join fetch bh.ticket t
				left join fetch bh.purchasePlan pp
				where bh.company.id = :companyId
					and (:departmentId is null or d.id = :departmentId)
					and (:budgetYear is null or b.budgetYear = :budgetYear)
					and (:historyType is null or bh.historyType = :historyType)
					and (
						:keyword is null
						or lower(coalesce(bh.description, '')) like :keyword
						or lower(coalesce(d.name, '')) like :keyword
						or lower(coalesce(t.ticketNo, '')) like :keyword
						or lower(coalesce(pp.planNo, '')) like :keyword
					)
				order by bh.createdAt desc
				""", BudgetHistory.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", request.getDepartmentId())
			.setParameter("budgetYear", request.getBudgetYear())
			.setParameter("historyType", request.getHistoryType())
			.setParameter("keyword", keyword)
			.setFirstResult((int) request.toPageable().getOffset())
			.setMaxResults(request.getSize())
			.getResultList()
			.stream()
			.map(this::toBudgetLedgerResponse)
			.toList();

		Long total = entityManager.createQuery("""
				select count(bh)
				from BudgetHistory bh
				join bh.budget b
				left join bh.department d
				left join bh.ticket t
				left join bh.purchasePlan pp
				where bh.company.id = :companyId
					and (:departmentId is null or d.id = :departmentId)
					and (:budgetYear is null or b.budgetYear = :budgetYear)
					and (:historyType is null or bh.historyType = :historyType)
					and (
						:keyword is null
						or lower(coalesce(bh.description, '')) like :keyword
						or lower(coalesce(d.name, '')) like :keyword
						or lower(coalesce(t.ticketNo, '')) like :keyword
						or lower(coalesce(pp.planNo, '')) like :keyword
					)
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", request.getDepartmentId())
			.setParameter("budgetYear", request.getBudgetYear())
			.setParameter("historyType", request.getHistoryType())
			.setParameter("keyword", keyword)
			.getSingleResult();

		Page<BudgetLedgerResponse> page = new PageImpl<>(content, request.toPageable(), total);
		return PaginationResponse.from(page);
	}

	private long countTickets(UUID companyId, UUID departmentId, List<TicketStatus> statuses) {
		return entityManager.createQuery("""
				select count(t)
				from Ticket t
				where t.company.id = :companyId
					and (:departmentId is null or t.department.id = :departmentId)
					and t.ticketStatus in :statuses
					and t.deletedAt is null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("statuses", statuses)
			.getSingleResult();
	}

	private long countMemberTickets(UUID companyId, UUID memberId, List<TicketStatus> statuses) {
		return entityManager.createQuery("""
				select count(t)
				from Ticket t
				where t.company.id = :companyId
					and t.requester.id = :memberId
					and t.ticketStatus in :statuses
					and t.deletedAt is null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("memberId", memberId)
			.setParameter("statuses", statuses)
			.getSingleResult();
	}

	private long countMemberRentalScheduledTickets(UUID companyId, UUID memberId) {
		return entityManager.createQuery("""
				select count(rt)
				from RentalTicket rt
				where rt.company.id = :companyId
					and rt.ticket.requester.id = :memberId
					and rt.status = :status
					and rt.deletedAt is null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("memberId", memberId)
			.setParameter("status", RentalTicketStatus.RESERVED)
			.getSingleResult();
	}

	private PaginationResponse<OwnedAssetDetailResponse> getUnassignedAssetDetails(
		UUID companyId,
		UUID departmentId,
		String keyword,
		PaginationRequest request
	) {
		String normalizedKeyword = normalizeKeyword(keyword);
		String keywordPattern = toKeywordPattern(normalizedKeyword);
		List<Object[]> rows = entityManager.createQuery("""
				select a.id, i.productName, c.name, a.assetCode, a.warrantyExpiredAt
				from TangibleAsset a
				join a.tangibleAssetItem i
				join i.tangibleAssetCategory c
				where a.company.id = :companyId
					and (:departmentId is null or a.department.id = :departmentId)
					and a.tangibleAssetStatus = :status
					and (:keyword is null
						or lower(i.productName) like :keyword
						or lower(c.name) like :keyword
						or lower(a.assetCode) like :keyword)
				order by a.createdAt desc
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("status", TangibleAssetStatus.AVAILABLE)
			.setParameter("keyword", keywordPattern)
			.setFirstResult((int) request.toPageable().getOffset())
			.setMaxResults(request.getSize())
			.getResultList();
		Long total = entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				join a.tangibleAssetItem i
				join i.tangibleAssetCategory c
				where a.company.id = :companyId
					and (:departmentId is null or a.department.id = :departmentId)
					and a.tangibleAssetStatus = :status
					and (:keyword is null
						or lower(i.productName) like :keyword
						or lower(c.name) like :keyword
						or lower(a.assetCode) like :keyword)
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("status", TangibleAssetStatus.AVAILABLE)
			.setParameter("keyword", keywordPattern)
			.getSingleResult();
		List<OwnedAssetDetailResponse> content = rows.stream()
			.map(row -> OwnedAssetDetailResponse.builder()
				.assetType(AssetType.TANGIBLE)
				.assetId((UUID) row[0])
				.assetName((String) row[1])
				.categoryName((String) row[2])
				.categoryOrProvider((String) row[2])
				.assetCode((String) row[3])
				.warrantyExpiredAt((LocalDateTime) row[4])
				.dueDate((LocalDateTime) row[4])
				.dayCount(calculateDashboardDayCount((LocalDateTime) row[4], LocalDateTime.now()))
				.dayStatusLabel(resolveDashboardDayStatusLabel((LocalDateTime) row[4], LocalDateTime.now()))
				.build())
			.toList();
		return toPaginationResponse(content, request, total);
	}

	private PaginationResponse<OwnedAssetDetailResponse> getRentalScheduledAssetDetails(
		UUID companyId,
		UUID memberId,
		UUID departmentId,
		String keyword,
		PaginationRequest request
	) {
		String keywordPattern = toKeywordPattern(normalizeKeyword(keyword));
		List<Object[]> rows = entityManager.createQuery("""
				select a.id, i.productName, a.assetCode, c.name, d.id, d.name, m.id, m.name, rt.rentalStartDate, rt.requestedDueDate
				from RentalTicket rt
				join rt.ticket t
				join rt.tangibleAsset a
				join a.tangibleAssetItem i
				join i.tangibleAssetCategory c
				join t.department d
				join t.requester m
				where rt.company.id = :companyId
					and rt.status = :status
					and rt.deletedAt is null
					and (:memberId is null or m.id = :memberId)
					and (:departmentId is null or d.id = :departmentId)
					and (:keyword is null
						or lower(i.productName) like :keyword
						or lower(a.assetCode) like :keyword
						or lower(d.name) like :keyword
						or lower(m.name) like :keyword)
				order by rt.rentalStartDate asc
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("status", RentalTicketStatus.RESERVED)
			.setParameter("memberId", memberId)
			.setParameter("departmentId", departmentId)
			.setParameter("keyword", keywordPattern)
			.setFirstResult((int) request.toPageable().getOffset())
			.setMaxResults(request.getSize())
			.getResultList();
		Long total = countRentalScheduledAssetDetails(companyId, memberId, departmentId, keywordPattern);
		List<OwnedAssetDetailResponse> content = rows.stream()
			.map(row -> {
				LocalDateTime returnDueDate = (LocalDateTime) row[9];
				return OwnedAssetDetailResponse.builder()
					.assetType(AssetType.TANGIBLE)
					.assetId((UUID) row[0])
					.assetName((String) row[1])
					.assetCode((String) row[2])
					.categoryName((String) row[3])
					.categoryOrProvider((String) row[3])
					.departmentId((UUID) row[4])
					.departmentName((String) row[5])
					.renterId((UUID) row[6])
					.renterName((String) row[7])
					.usedStartedAt((LocalDateTime) row[8])
					.returnDueDate(returnDueDate)
					.dueDate(returnDueDate)
					.dayCount(calculateDashboardDayCount(returnDueDate, LocalDateTime.now()))
					.dayStatusLabel(resolveDashboardDayStatusLabel(returnDueDate, LocalDateTime.now()))
					.build();
			})
			.toList();
		return toPaginationResponse(content, request, total);
	}

	private Long countRentalScheduledAssetDetails(UUID companyId, UUID memberId, UUID departmentId, String keywordPattern) {
		return entityManager.createQuery("""
				select count(rt)
				from RentalTicket rt
				join rt.ticket t
				join rt.tangibleAsset a
				join a.tangibleAssetItem i
				join t.department d
				join t.requester m
				where rt.company.id = :companyId
					and rt.status = :status
					and rt.deletedAt is null
					and (:memberId is null or m.id = :memberId)
					and (:departmentId is null or d.id = :departmentId)
					and (:keyword is null
						or lower(i.productName) like :keyword
						or lower(a.assetCode) like :keyword
						or lower(d.name) like :keyword
						or lower(m.name) like :keyword)
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("status", RentalTicketStatus.RESERVED)
			.setParameter("memberId", memberId)
			.setParameter("departmentId", departmentId)
			.setParameter("keyword", keywordPattern)
			.getSingleResult();
	}

	private PaginationResponse<OwnedAssetDetailResponse> getRentedAssetDetails(
		UUID companyId,
		UUID memberId,
		UUID departmentId,
		String keyword,
		PaginationRequest request
	) {
		return getInUseTemporaryAssetDetails(companyId, memberId, departmentId, keyword, false, request);
	}

	private PaginationResponse<OwnedAssetDetailResponse> getOverdueAssetDetails(
		UUID companyId,
		UUID memberId,
		UUID departmentId,
		String keyword,
		PaginationRequest request
	) {
		return getInUseTemporaryAssetDetails(companyId, memberId, departmentId, keyword, true, request);
	}

	private PaginationResponse<OwnedAssetDetailResponse> getInUseTemporaryAssetDetails(
		UUID companyId,
		UUID memberId,
		UUID departmentId,
		String keyword,
		boolean overdueOnly,
		PaginationRequest request
	) {
		LocalDateTime now = LocalDateTime.now();
		String keywordPattern = toKeywordPattern(normalizeKeyword(keyword));
		List<Object[]> rows = entityManager.createQuery("""
				select a.id, i.productName, a.assetCode, c.name, d.id, d.name, m.id, m.name, a.usedStartedAt, a.returnDueDate
				from TangibleAsset a
				join a.tangibleAssetItem i
				join i.tangibleAssetCategory c
				join a.department d
				join a.member m
				where a.company.id = :companyId
					and a.tangibleAssetStatus = :status
					and a.usageType = :usageType
					and (:memberId is null or m.id = :memberId)
					and (:departmentId is null or d.id = :departmentId)
					and (
						(:overdueOnly = true and a.returnDueDate < :now)
						or (:overdueOnly = false and (a.returnDueDate is null or a.returnDueDate >= :now))
					)
					and (:keyword is null
						or lower(i.productName) like :keyword
						or lower(a.assetCode) like :keyword
						or lower(d.name) like :keyword
						or lower(m.name) like :keyword)
				order by a.returnDueDate asc
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.setParameter("usageType", UsageType.TEMPORARY)
			.setParameter("memberId", memberId)
			.setParameter("departmentId", departmentId)
			.setParameter("overdueOnly", overdueOnly)
			.setParameter("now", now)
			.setParameter("keyword", keywordPattern)
			.setFirstResult((int) request.toPageable().getOffset())
			.setMaxResults(request.getSize())
			.getResultList();
		Long total = countInUseTemporaryAssetDetails(companyId, memberId, departmentId, keywordPattern, overdueOnly, now);
		List<OwnedAssetDetailResponse> content = rows.stream()
			.map(row -> {
				LocalDateTime returnDueDate = (LocalDateTime) row[9];
				return OwnedAssetDetailResponse.builder()
					.assetType(AssetType.TANGIBLE)
					.assetId((UUID) row[0])
					.assetName((String) row[1])
					.assetCode((String) row[2])
					.categoryName((String) row[3])
					.categoryOrProvider((String) row[3])
					.departmentId((UUID) row[4])
					.departmentName((String) row[5])
					.renterId((UUID) row[6])
					.renterName((String) row[7])
					.usedStartedAt((LocalDateTime) row[8])
					.returnDueDate(returnDueDate)
					.dueDate(returnDueDate)
					.dayCount(calculateDashboardDayCount(returnDueDate, now))
					.dayStatusLabel(resolveDashboardDayStatusLabel(returnDueDate, now))
					.overdueDays(overdueOnly && returnDueDate != null ? ChronoUnit.DAYS.between(returnDueDate, now) : null)
					.build();
			})
			.toList();
		return toPaginationResponse(content, request, total);
	}

	private Long countInUseTemporaryAssetDetails(
		UUID companyId,
		UUID memberId,
		UUID departmentId,
		String keywordPattern,
		boolean overdueOnly,
		LocalDateTime now
	) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				join a.tangibleAssetItem i
				join a.department d
				join a.member m
				where a.company.id = :companyId
					and a.tangibleAssetStatus = :status
					and a.usageType = :usageType
					and (:memberId is null or m.id = :memberId)
					and (:departmentId is null or d.id = :departmentId)
					and (
						(:overdueOnly = true and a.returnDueDate < :now)
						or (:overdueOnly = false and (a.returnDueDate is null or a.returnDueDate >= :now))
					)
					and (:keyword is null
						or lower(i.productName) like :keyword
						or lower(a.assetCode) like :keyword
						or lower(d.name) like :keyword
						or lower(m.name) like :keyword)
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.setParameter("usageType", UsageType.TEMPORARY)
			.setParameter("memberId", memberId)
			.setParameter("departmentId", departmentId)
			.setParameter("overdueOnly", overdueOnly)
			.setParameter("now", now)
			.setParameter("keyword", keywordPattern)
			.getSingleResult();
	}

	private long countMemberRentedTangibleAssets(UUID companyId, UUID memberId) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				where a.company.id = :companyId
					and a.member.id = :memberId
					and a.tangibleAssetStatus = :status
					and a.usageType = :usageType
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("memberId", memberId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.setParameter("usageType", UsageType.TEMPORARY)
			.getSingleResult();
	}

	private long countMemberOverdueTangibleAssets(UUID companyId, UUID memberId, LocalDateTime now) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				where a.company.id = :companyId
					and a.member.id = :memberId
					and a.tangibleAssetStatus = :status
					and a.usageType = :usageType
					and a.returnDueDate < :now
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("memberId", memberId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.setParameter("usageType", UsageType.TEMPORARY)
			.setParameter("now", now)
			.getSingleResult();
	}

	private long countMemberExpiringTangibleAssets(
		UUID companyId,
		UUID memberId,
		LocalDateTime now,
		LocalDateTime limit
	) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				where a.company.id = :companyId
					and a.member.id = :memberId
					and a.warrantyExpiredAt between :now and :limit
					and a.tangibleAssetStatus <> :disposed
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("memberId", memberId)
			.setParameter("now", now)
			.setParameter("limit", limit)
			.setParameter("disposed", TangibleAssetStatus.DISPOSED)
			.getSingleResult();
	}

	private long countMemberExpiringIntangibleAssets(
		UUID companyId,
		UUID memberId,
		LocalDateTime now,
		LocalDateTime limit
	) {
		return entityManager.createQuery("""
				select count(a)
				from IntangibleAsset a
				where a.company.id = :companyId
					and a.member.id = :memberId
					and a.expiredAt between :now and :limit
					and a.intangibleAssetStatus not in :excludedStatuses
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("memberId", memberId)
			.setParameter("now", now)
			.setParameter("limit", limit)
			.setParameter("excludedStatuses", List.of(
				IntangibleAssetStatus.EXPIRED,
				IntangibleAssetStatus.CANCELLED
			))
			.getSingleResult();
	}

	private long countAvailableTangibleAssets(UUID companyId, UUID departmentId) {
		return countTangibleAssetsByStatus(companyId, departmentId, TangibleAssetStatus.AVAILABLE);
	}

	private long countAvailableIntangibleAssets(UUID companyId, UUID departmentId) {
		return entityManager.createQuery("""
				select coalesce(sum(a.seatCount), 0)
				from IntangibleAsset a
				where a.company.id = :companyId
					and (:departmentId is null or a.department.id = :departmentId)
					and a.intangibleAssetStatus = :status
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("status", IntangibleAssetStatus.AVAILABLE)
			.getSingleResult();
	}

	private long countTangibleAssetsByStatus(UUID companyId, UUID departmentId, TangibleAssetStatus status) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				where a.company.id = :companyId
					and (:departmentId is null or a.department.id = :departmentId)
					and a.tangibleAssetStatus = :status
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("status", status)
			.getSingleResult();
	}

	private long countRentedTangibleAssets(UUID companyId, UUID departmentId) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				where a.company.id = :companyId
					and (:departmentId is null or a.department.id = :departmentId)
					and a.tangibleAssetStatus = :status
					and a.usageType = :usageType
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.setParameter("usageType", UsageType.TEMPORARY)
			.getSingleResult();
	}

	private long countOverdueTangibleAssets(UUID companyId, UUID departmentId, LocalDateTime now) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				where a.company.id = :companyId
					and (:departmentId is null or a.department.id = :departmentId)
					and a.tangibleAssetStatus = :status
					and a.usageType = :usageType
					and a.returnDueDate < :now
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.setParameter("usageType", UsageType.TEMPORARY)
			.setParameter("now", now)
			.getSingleResult();
	}

	private long countExpiringTangibleAssets(UUID companyId, UUID departmentId, LocalDateTime now, LocalDateTime limit) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				where a.company.id = :companyId
					and (:departmentId is null or a.department.id = :departmentId)
					and a.warrantyExpiredAt between :now and :limit
					and a.tangibleAssetStatus <> :disposed
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("now", now)
			.setParameter("limit", limit)
			.setParameter("disposed", TangibleAssetStatus.DISPOSED)
			.getSingleResult();
	}

	private PaginationResponse<ExpiringAssetDetailResponse> getAllExpiringAssetDetails(
		UUID companyId,
		UUID memberId,
		UUID departmentId,
		String keyword,
		LocalDateTime now,
		LocalDateTime limit,
		PaginationRequest request
	) {
		PaginationRequest allRequest = new PaginationRequest();
		allRequest.setPage(0);
		allRequest.setSize(Integer.MAX_VALUE);

		List<ExpiringAssetDetailResponse> content = new ArrayList<>();
		content.addAll(getExpiringTangibleAssetDetails(
			companyId,
			memberId,
			departmentId,
			keyword,
			now,
			limit,
			allRequest
		).getContent());
		content.addAll(getExpiringIntangibleAssetDetails(
			companyId,
			memberId,
			departmentId,
			keyword,
			now,
			limit,
			allRequest
		).getContent());
		content.sort(Comparator.comparing(
			ExpiringAssetDetailResponse::getDueDate,
			Comparator.nullsLast(Comparator.naturalOrder())
		));
		return toPaginationResponse(content, request);
	}

	private PaginationResponse<ExpiringAssetDetailResponse> getExpiringTangibleAssetDetails(
		UUID companyId,
		UUID memberId,
		UUID departmentId,
		String keyword,
		LocalDateTime now,
		LocalDateTime limit,
		PaginationRequest request
	) {
		String keywordPattern = toKeywordPattern(normalizeKeyword(keyword));
		List<Object[]> rows = entityManager.createQuery("""
				select a.id, i.productName, d.id, d.name, m.id, m.name, a.warrantyExpiredAt, a.assetCode, i.manufacturer, c.name
				from TangibleAsset a
				join a.tangibleAssetItem i
				join i.tangibleAssetCategory c
				left join a.department d
				left join a.member m
				where a.company.id = :companyId
					and (:memberId is null or m.id = :memberId)
					and (:departmentId is null or d.id = :departmentId)
					and a.warrantyExpiredAt between :now and :limit
					and a.tangibleAssetStatus <> :disposed
					and (:keyword is null
						or lower(i.productName) like :keyword
						or lower(a.assetCode) like :keyword
						or lower(c.name) like :keyword
						or lower(i.manufacturer) like :keyword
						or lower(d.name) like :keyword
						or lower(m.name) like :keyword)
				order by a.warrantyExpiredAt asc
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("memberId", memberId)
			.setParameter("departmentId", departmentId)
			.setParameter("now", now)
			.setParameter("limit", limit)
			.setParameter("disposed", TangibleAssetStatus.DISPOSED)
			.setParameter("keyword", keywordPattern)
			.setFirstResult((int) request.toPageable().getOffset())
			.setMaxResults(request.getSize())
			.getResultList();
		Long total = countExpiringTangibleAssetDetails(companyId, memberId, departmentId, keywordPattern, now, limit);
		List<ExpiringAssetDetailResponse> content = rows.stream()
			.map(row -> {
				LocalDateTime expiredAt = (LocalDateTime) row[6];
				return ExpiringAssetDetailResponse.builder()
					.assetType(AssetType.TANGIBLE)
					.assetId((UUID) row[0])
					.assetName((String) row[1])
					.departmentId((UUID) row[2])
					.departmentName((String) row[3])
					.userId((UUID) row[4])
					.userName((String) row[5])
					.expiredAt(expiredAt)
					.remainingDays(ChronoUnit.DAYS.between(now, expiredAt))
					.expirationDate(expiredAt)
					.remainingPeriodDays(calculateDashboardDayCount(expiredAt, now))
					.remainingPeriodStatus(resolveExpirationPeriodStatus(expiredAt, now))
					.dueDate(expiredAt)
					.dayCount(ChronoUnit.DAYS.between(now, expiredAt))
					.dayStatusLabel("REMAINING")
					.assetCode((String) row[7])
					.categoryOrProvider((String) row[9])
					.manufacturer((String) row[8])
					.build();
			})
			.toList();
		return toPaginationResponse(content, request, total);
	}

	private Long countExpiringTangibleAssetDetails(
		UUID companyId,
		UUID memberId,
		UUID departmentId,
		String keywordPattern,
		LocalDateTime now,
		LocalDateTime limit
	) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				join a.tangibleAssetItem i
				join i.tangibleAssetCategory c
				left join a.department d
				left join a.member m
				where a.company.id = :companyId
					and (:memberId is null or m.id = :memberId)
					and (:departmentId is null or d.id = :departmentId)
					and a.warrantyExpiredAt between :now and :limit
					and a.tangibleAssetStatus <> :disposed
					and (:keyword is null
						or lower(i.productName) like :keyword
						or lower(a.assetCode) like :keyword
						or lower(c.name) like :keyword
						or lower(i.manufacturer) like :keyword
						or lower(d.name) like :keyword
						or lower(m.name) like :keyword)
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("memberId", memberId)
			.setParameter("departmentId", departmentId)
			.setParameter("now", now)
			.setParameter("limit", limit)
			.setParameter("disposed", TangibleAssetStatus.DISPOSED)
			.setParameter("keyword", keywordPattern)
			.getSingleResult();
	}

	private long countExpiringIntangibleAssets(UUID companyId, UUID departmentId, LocalDateTime now, LocalDateTime limit) {
		return entityManager.createQuery("""
				select count(a)
				from IntangibleAsset a
				where a.company.id = :companyId
					and (:departmentId is null or a.department.id = :departmentId)
					and a.expiredAt between :now and :limit
					and a.intangibleAssetStatus not in :excludedStatuses
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("now", now)
			.setParameter("limit", limit)
			.setParameter("excludedStatuses", List.of(
				IntangibleAssetStatus.EXPIRED,
				IntangibleAssetStatus.CANCELLED
			))
			.getSingleResult();
	}

	private PaginationResponse<ExpiringAssetDetailResponse> getExpiringIntangibleAssetDetails(
		UUID companyId,
		UUID memberId,
		UUID departmentId,
		String keyword,
		LocalDateTime now,
		LocalDateTime limit,
		PaginationRequest request
	) {
		String keywordPattern = toKeywordPattern(normalizeKeyword(keyword));
		List<Object[]> rows = entityManager.createQuery("""
				select a.id, i.productName, d.id, d.name, m.id, m.name, a.expiredAt, a.assetCode, i.provider
				from IntangibleAsset a
				join a.intangibleAssetItem i
				left join a.department d
				left join a.member m
				where a.company.id = :companyId
					and (:memberId is null or m.id = :memberId)
					and (:departmentId is null or d.id = :departmentId)
					and a.expiredAt between :now and :limit
					and a.intangibleAssetStatus not in :excludedStatuses
					and (:keyword is null
						or lower(i.productName) like :keyword
						or lower(a.assetCode) like :keyword
						or lower(i.provider) like :keyword
						or lower(d.name) like :keyword
						or lower(m.name) like :keyword)
				order by a.expiredAt asc
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("memberId", memberId)
			.setParameter("departmentId", departmentId)
			.setParameter("now", now)
			.setParameter("limit", limit)
			.setParameter("excludedStatuses", List.of(
				IntangibleAssetStatus.EXPIRED,
				IntangibleAssetStatus.CANCELLED
			))
			.setParameter("keyword", keywordPattern)
			.setFirstResult((int) request.toPageable().getOffset())
			.setMaxResults(request.getSize())
			.getResultList();
		Long total = countExpiringIntangibleAssetDetails(companyId, memberId, departmentId, keywordPattern, now, limit);
		List<ExpiringAssetDetailResponse> content = rows.stream()
			.map(row -> {
				LocalDateTime expiredAt = (LocalDateTime) row[6];
				return ExpiringAssetDetailResponse.builder()
					.assetType(AssetType.INTANGIBLE)
					.assetId((UUID) row[0])
					.assetName((String) row[1])
					.departmentId((UUID) row[2])
					.departmentName((String) row[3])
					.userId((UUID) row[4])
					.userName((String) row[5])
					.expiredAt(expiredAt)
					.remainingDays(ChronoUnit.DAYS.between(now, expiredAt))
					.expirationDate(expiredAt)
					.remainingPeriodDays(calculateDashboardDayCount(expiredAt, now))
					.remainingPeriodStatus(resolveExpirationPeriodStatus(expiredAt, now))
					.dueDate(expiredAt)
					.dayCount(ChronoUnit.DAYS.between(now, expiredAt))
					.dayStatusLabel("REMAINING")
					.assetCode((String) row[7])
					.categoryOrProvider((String) row[8])
					.issuer((String) row[8])
					.build();
			})
			.toList();
		return toPaginationResponse(content, request, total);
	}

	private Long countExpiringIntangibleAssetDetails(
		UUID companyId,
		UUID memberId,
		UUID departmentId,
		String keywordPattern,
		LocalDateTime now,
		LocalDateTime limit
	) {
		return entityManager.createQuery("""
				select count(a)
				from IntangibleAsset a
				join a.intangibleAssetItem i
				left join a.department d
				left join a.member m
				where a.company.id = :companyId
					and (:memberId is null or m.id = :memberId)
					and (:departmentId is null or d.id = :departmentId)
					and a.expiredAt between :now and :limit
					and a.intangibleAssetStatus not in :excludedStatuses
					and (:keyword is null
						or lower(i.productName) like :keyword
						or lower(a.assetCode) like :keyword
						or lower(i.provider) like :keyword
						or lower(d.name) like :keyword
						or lower(m.name) like :keyword)
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("memberId", memberId)
			.setParameter("departmentId", departmentId)
			.setParameter("now", now)
			.setParameter("limit", limit)
			.setParameter("excludedStatuses", List.of(
				IntangibleAssetStatus.EXPIRED,
				IntangibleAssetStatus.CANCELLED
			))
			.setParameter("keyword", keywordPattern)
			.getSingleResult();
	}

	private List<AssetDemandResponse> getTangibleAssetDemands(UUID companyId) {
		List<Object[]> items = entityManager.createQuery("""
				select i.id, i.productName
				from TangibleAssetItem i
				where i.company.id = :companyId
					and i.deletedAt is null
				""", Object[].class)
			.setParameter("companyId", companyId)
			.getResultList();

		return items.stream()
			.map(row -> {
				UUID itemId = (UUID) row[0];
				String itemName = (String) row[1];
				long expectedDemand = countTangibleExpectedDemand(companyId, itemId);
				long currentInventory = countTangibleCurrentInventory(companyId, itemId);
				long scheduledReturn = countTangibleScheduledReturn(companyId, itemId);
				return buildAssetDemand(TANGIBLE, itemId, itemName, expectedDemand, currentInventory, scheduledReturn);
			})
			.toList();
	}

	private List<AssetDemandResponse> getIntangibleAssetDemands(UUID companyId) {
		List<Object[]> items = entityManager.createQuery("""
				select i.id, i.productName
				from IntangibleAssetItem i
				where i.company.id = :companyId
					and i.deletedAt is null
				""", Object[].class)
			.setParameter("companyId", companyId)
			.getResultList();

		return items.stream()
			.map(row -> {
				UUID itemId = (UUID) row[0];
				String itemName = (String) row[1];
				long expectedDemand = countIntangibleExpectedDemand(companyId, itemId);
				long currentInventory = countIntangibleCurrentInventory(companyId, itemId);
				long scheduledReturn = countIntangibleScheduledReturn(companyId, itemId);
				return buildAssetDemand(INTANGIBLE, itemId, itemName, expectedDemand, currentInventory, scheduledReturn);
			})
			.toList();
	}

	private List<AssetDemandResponse> getDepartmentTangibleAssetDemands(UUID companyId, UUID departmentId) {
		List<Object[]> items = entityManager.createQuery("""
				select distinct i.id, i.productName
				from TangibleAssetItem i
				where i.company.id = :companyId
					and i.deletedAt is null
				""", Object[].class)
			.setParameter("companyId", companyId)
			.getResultList();

		return items.stream()
			.map(row -> {
				UUID itemId = (UUID) row[0];
				String itemName = (String) row[1];
				long expectedDemand = countDepartmentTangibleExpectedDemand(companyId, departmentId, itemId);
				long currentInventory = countDepartmentTangibleCurrentInventory(companyId, departmentId, itemId);
				long scheduledReturn = countDepartmentTangibleScheduledReturn(companyId, departmentId, itemId);
				return buildAssetDemand(TANGIBLE, itemId, itemName, expectedDemand, currentInventory, scheduledReturn);
			})
			.toList();
	}

	private List<AssetDemandResponse> getDepartmentIntangibleAssetDemands(UUID companyId, UUID departmentId) {
		List<Object[]> items = entityManager.createQuery("""
				select distinct i.id, i.productName
				from IntangibleAssetItem i
				where i.company.id = :companyId
					and i.deletedAt is null
				""", Object[].class)
			.setParameter("companyId", companyId)
			.getResultList();

		return items.stream()
			.map(row -> {
				UUID itemId = (UUID) row[0];
				String itemName = (String) row[1];
				long expectedDemand = countDepartmentIntangibleExpectedDemand(companyId, departmentId, itemId);
				long currentInventory = countDepartmentIntangibleCurrentInventory(companyId, departmentId, itemId);
				long scheduledReturn = countDepartmentIntangibleScheduledReturn(companyId, departmentId, itemId);
				return buildAssetDemand(INTANGIBLE, itemId, itemName, expectedDemand, currentInventory, scheduledReturn);
			})
			.toList();
	}

	private AssetDemandResponse buildAssetDemand(
		String assetType,
		UUID itemId,
		String assetName,
		long expectedDemand,
		long currentInventory,
		long scheduledReturn
	) {
		BigDecimal availabilityRate = expectedDemand == 0
			? BigDecimal.valueOf(100)
			: BigDecimal.valueOf(currentInventory)
				.multiply(BigDecimal.valueOf(100))
				.divide(BigDecimal.valueOf(expectedDemand), 1, RoundingMode.HALF_UP);

		return AssetDemandResponse.builder()
			.assetType(assetType)
			.itemId(itemId)
			.assetName(assetName)
			.expectedDemand(expectedDemand)
			.currentInventory(currentInventory)
			.scheduledReturn(scheduledReturn)
			.availabilityRate(availabilityRate)
			.status(currentInventory < expectedDemand ? "부족" : "충분")
			.build();
	}

	private long countTangibleExpectedDemand(UUID companyId, UUID itemId) {
		return entityManager.createQuery("""
				select coalesce(sum(t.quantity), 0)
				from AssetRequestTicket t
				where t.company.id = :companyId
					and t.tangibleAssetItem.id = :itemId
					and t.status not in :excludedStatuses
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("itemId", itemId)
			.setParameter("excludedStatuses", List.of(
				AssetRequestTicketStatus.COMPLETED,
				AssetRequestTicketStatus.CANCELLED
			))
			.getSingleResult();
	}

	private long countIntangibleExpectedDemand(UUID companyId, UUID itemId) {
		return entityManager.createQuery("""
				select coalesce(sum(t.quantity), 0)
				from AssetRequestTicket t
				where t.company.id = :companyId
					and t.intangibleAssetItem.id = :itemId
					and t.status not in :excludedStatuses
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("itemId", itemId)
			.setParameter("excludedStatuses", List.of(
				AssetRequestTicketStatus.COMPLETED,
				AssetRequestTicketStatus.CANCELLED
			))
			.getSingleResult();
	}

	private long countDepartmentTangibleExpectedDemand(UUID companyId, UUID departmentId, UUID itemId) {
		return entityManager.createQuery("""
				select coalesce(sum(t.quantity), 0)
				from AssetRequestTicket t
				where t.company.id = :companyId
					and t.ticket.department.id = :departmentId
					and t.tangibleAssetItem.id = :itemId
					and t.status not in :excludedStatuses
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("itemId", itemId)
			.setParameter("excludedStatuses", List.of(
				AssetRequestTicketStatus.COMPLETED,
				AssetRequestTicketStatus.CANCELLED
			))
			.getSingleResult();
	}

	private long countDepartmentIntangibleExpectedDemand(UUID companyId, UUID departmentId, UUID itemId) {
		return entityManager.createQuery("""
				select coalesce(sum(t.quantity), 0)
				from AssetRequestTicket t
				where t.company.id = :companyId
					and t.ticket.department.id = :departmentId
					and t.intangibleAssetItem.id = :itemId
					and t.status not in :excludedStatuses
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("itemId", itemId)
			.setParameter("excludedStatuses", List.of(
				AssetRequestTicketStatus.COMPLETED,
				AssetRequestTicketStatus.CANCELLED
			))
			.getSingleResult();
	}

	private long countTangibleCurrentInventory(UUID companyId, UUID itemId) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				where a.company.id = :companyId
					and a.tangibleAssetItem.id = :itemId
					and a.tangibleAssetStatus = :status
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("itemId", itemId)
			.setParameter("status", TangibleAssetStatus.AVAILABLE)
			.getSingleResult();
	}

	private long countIntangibleCurrentInventory(UUID companyId, UUID itemId) {
		Long seatCount = entityManager.createQuery("""
				select coalesce(sum(a.seatCount), 0)
				from IntangibleAsset a
				where a.company.id = :companyId
					and a.intangibleAssetItem.id = :itemId
					and a.intangibleAssetStatus in :statuses
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("itemId", itemId)
			.setParameter("statuses", List.of(
				IntangibleAssetStatus.AVAILABLE,
				IntangibleAssetStatus.IN_USE
			))
			.getSingleResult();

		Long activeAssignments = entityManager.createQuery("""
				select count(aa)
				from IntangibleAssetAssignment aa
				where aa.company.id = :companyId
					and aa.intangibleAsset.intangibleAssetItem.id = :itemId
					and aa.assignmentStatus = :status
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("itemId", itemId)
			.setParameter("status", AssignmentStatus.ACTIVE)
			.getSingleResult();

		return Math.max(seatCount - activeAssignments, 0);
	}

	private long countDepartmentTangibleCurrentInventory(UUID companyId, UUID departmentId, UUID itemId) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				where a.company.id = :companyId
					and a.department.id = :departmentId
					and a.tangibleAssetItem.id = :itemId
					and a.tangibleAssetStatus = :status
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("itemId", itemId)
			.setParameter("status", TangibleAssetStatus.AVAILABLE)
			.getSingleResult();
	}

	private long countDepartmentIntangibleCurrentInventory(UUID companyId, UUID departmentId, UUID itemId) {
		Long seatCount = entityManager.createQuery("""
				select coalesce(sum(a.seatCount), 0)
				from IntangibleAsset a
				where a.company.id = :companyId
					and a.department.id = :departmentId
					and a.intangibleAssetItem.id = :itemId
					and a.intangibleAssetStatus in :statuses
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("itemId", itemId)
			.setParameter("statuses", List.of(
				IntangibleAssetStatus.AVAILABLE,
				IntangibleAssetStatus.IN_USE
			))
			.getSingleResult();

		Long activeAssignments = entityManager.createQuery("""
				select count(aa)
				from IntangibleAssetAssignment aa
				where aa.company.id = :companyId
					and aa.department.id = :departmentId
					and aa.intangibleAsset.intangibleAssetItem.id = :itemId
					and aa.assignmentStatus = :status
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("itemId", itemId)
			.setParameter("status", AssignmentStatus.ACTIVE)
			.getSingleResult();

		return Math.max(seatCount - activeAssignments, 0);
	}

	private long countTangibleScheduledReturn(UUID companyId, UUID itemId) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				where a.company.id = :companyId
					and a.tangibleAssetItem.id = :itemId
					and a.tangibleAssetStatus = :status
					and a.returnDueDate is not null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("itemId", itemId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.getSingleResult();
	}

	private long countIntangibleScheduledReturn(UUID companyId, UUID itemId) {
		return entityManager.createQuery("""
				select coalesce(sum(a.seatCount), 0)
				from IntangibleAsset a
				where a.company.id = :companyId
					and a.intangibleAssetItem.id = :itemId
					and a.intangibleAssetStatus = :status
					and a.expiredAt is not null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("itemId", itemId)
			.setParameter("status", IntangibleAssetStatus.IN_USE)
			.getSingleResult();
	}

	private long countDepartmentTangibleScheduledReturn(UUID companyId, UUID departmentId, UUID itemId) {
		return entityManager.createQuery("""
				select count(a)
				from TangibleAsset a
				where a.company.id = :companyId
					and a.department.id = :departmentId
					and a.tangibleAssetItem.id = :itemId
					and a.tangibleAssetStatus = :status
					and a.returnDueDate is not null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("itemId", itemId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.getSingleResult();
	}

	private long countDepartmentIntangibleScheduledReturn(UUID companyId, UUID departmentId, UUID itemId) {
		return entityManager.createQuery("""
				select coalesce(sum(a.seatCount), 0)
				from IntangibleAsset a
				where a.company.id = :companyId
					and a.department.id = :departmentId
					and a.intangibleAssetItem.id = :itemId
					and a.intangibleAssetStatus = :status
					and a.expiredAt is not null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("itemId", itemId)
			.setParameter("status", IntangibleAssetStatus.IN_USE)
			.getSingleResult();
	}

	private BudgetOverviewResponse.CommonBudgetSummary findCommonBudget(UUID companyId, int year) {
		List<Budget> budgets = entityManager.createQuery("""
				select b
				from Budget b
				where b.company.id = :companyId
					and b.department is null
					and b.budgetYear = :year
				""", Budget.class)
			.setParameter("companyId", companyId)
			.setParameter("year", year)
			.getResultList();

		if (budgets.isEmpty()) {
			return BudgetOverviewResponse.CommonBudgetSummary.builder()
				.totalAmount(BigDecimal.ZERO)
				.remainingAmount(BigDecimal.ZERO)
				.remainingRate(BigDecimal.ZERO)
				.build();
		}

		Budget budget = budgets.get(0);
		return BudgetOverviewResponse.CommonBudgetSummary.builder()
			.totalAmount(budget.getTotalAmount())
			.remainingAmount(budget.getAvailableAmount())
			.remainingRate(percent(budget.getAvailableAmount(), budget.getTotalAmount()))
			.build();
	}

	private List<BudgetOverviewResponse.DepartmentBudgetSummary> findDepartmentBudgets(UUID companyId, int year) {
		return entityManager.createQuery("""
				select b
				from Budget b
				join fetch b.department d
				where b.company.id = :companyId
					and b.department is not null
					and b.budgetYear = :year
				order by d.name asc
				""", Budget.class)
			.setParameter("companyId", companyId)
			.setParameter("year", year)
			.getResultList()
			.stream()
			.map(budget -> BudgetOverviewResponse.DepartmentBudgetSummary.builder()
				.departmentId(budget.getDepartment().getId())
				.departmentName(budget.getDepartment().getName())
				.totalAmount(budget.getTotalAmount())
				.usedAmount(budget.getUsedAmount())
				.usageRate(percent(budget.getUsedAmount(), budget.getTotalAmount()))
				.build())
			.toList();
	}

	private List<LifecycleEventResponse> findReturnDueEvents(UUID companyId, LocalDateTime now, LocalDateTime limit) {
		return entityManager.createQuery("""
				select a.id, a.assetCode, i.productName, a.returnDueDate
				from TangibleAsset a
				join a.tangibleAssetItem i
				where a.company.id = :companyId
					and a.tangibleAssetStatus = :status
					and a.usageType = :usageType
					and a.returnDueDate is not null
					and a.returnDueDate <= :limit
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.setParameter("usageType", UsageType.TEMPORARY)
			.setParameter("limit", limit)
			.getResultList()
			.stream()
			.map(row -> lifecycleEvent(
				((LocalDateTime) row[3]).isBefore(now) ? "RETURN_OVERDUE" : "RETURN_DUE",
				TANGIBLE,
				(UUID) row[0],
				(String) row[1],
				(String) row[2],
				(LocalDateTime) row[3],
				now,
				((LocalDateTime) row[3]).isBefore(now) ? "연체" : "반납예정"
			))
			.toList();
	}

	private List<LifecycleEventResponse> findRentalScheduledEvents(UUID companyId, LocalDateTime now) {
		return entityManager.createQuery("""
				select a.id, a.assetCode, i.productName, a.updatedAt
				from TangibleAsset a
				join a.tangibleAssetItem i
				where a.company.id = :companyId
					and a.tangibleAssetStatus = :status
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("status", TangibleAssetStatus.RESERVED)
			.getResultList()
			.stream()
			.map(row -> lifecycleEvent(
				"RENTAL_SCHEDULED",
				TANGIBLE,
				(UUID) row[0],
				(String) row[1],
				(String) row[2],
				(LocalDateTime) row[3],
				now,
				"대여예정"
			))
			.toList();
	}

	private List<LifecycleEventResponse> findTangibleInUseEvents(UUID companyId, LocalDateTime now) {
		return entityManager.createQuery("""
				select a.id, a.assetCode, i.productName, a.returnDueDate, a.usageType
				from TangibleAsset a
				join a.tangibleAssetItem i
				where a.company.id = :companyId
					and a.tangibleAssetStatus = :status
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.getResultList()
			.stream()
			.map(row -> lifecycleEvent(
				UsageType.TEMPORARY.equals(row[4]) ? "RENTAL_IN_PROGRESS" : "ASSIGNED_IN_PROGRESS",
				TANGIBLE,
				(UUID) row[0],
				(String) row[1],
				(String) row[2],
				(LocalDateTime) row[3],
				now,
				UsageType.TEMPORARY.equals(row[4]) ? "대여중" : "사용중"
			))
			.toList();
	}

	private List<LifecycleEventResponse> findTangibleStatusEvents(UUID companyId, LocalDateTime now) {
		return entityManager.createQuery("""
				select a.id, a.assetCode, i.productName, a.updatedAt, a.tangibleAssetStatus
				from TangibleAsset a
				join a.tangibleAssetItem i
				where a.company.id = :companyId
					and a.tangibleAssetStatus in :statuses
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("statuses", List.of(
				TangibleAssetStatus.RETURN_REQUESTED,
				TangibleAssetStatus.REPAIR_REQUESTED,
				TangibleAssetStatus.REPAIRING,
				TangibleAssetStatus.DISPOSED
			))
			.getResultList()
			.stream()
			.map(row -> lifecycleEvent(
				toTangibleLifecycleEventType((TangibleAssetStatus) row[4]),
				TANGIBLE,
				(UUID) row[0],
				(String) row[1],
				(String) row[2],
				(LocalDateTime) row[3],
				now,
				toTangibleLifecycleStatus((TangibleAssetStatus) row[4])
			))
			.toList();
	}

	private List<LifecycleEventResponse> findTangibleWarrantyExpiringEvents(
		UUID companyId,
		LocalDateTime now,
		LocalDateTime limit
	) {
		return entityManager.createQuery("""
				select a.id, a.assetCode, i.productName, a.warrantyExpiredAt
				from TangibleAsset a
				join a.tangibleAssetItem i
				where a.company.id = :companyId
					and a.warrantyExpiredAt between :now and :limit
					and a.tangibleAssetStatus <> :disposed
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("now", now)
			.setParameter("limit", limit)
			.setParameter("disposed", TangibleAssetStatus.DISPOSED)
			.getResultList()
			.stream()
			.map(row -> lifecycleEvent(
				"WARRANTY_EXPIRING",
				TANGIBLE,
				(UUID) row[0],
				(String) row[1],
				(String) row[2],
				(LocalDateTime) row[3],
				now,
				"보증만료예정"
			))
			.toList();
	}

	private List<LifecycleEventResponse> findIntangibleInUseEvents(UUID companyId, LocalDateTime now) {
		return entityManager.createQuery("""
				select a.id, a.assetCode, i.productName, a.expiredAt
				from IntangibleAsset a
				join a.intangibleAssetItem i
				where a.company.id = :companyId
					and a.intangibleAssetStatus = :status
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("status", IntangibleAssetStatus.IN_USE)
			.getResultList()
			.stream()
			.map(row -> lifecycleEvent(
				"LICENSE_IN_PROGRESS",
				INTANGIBLE,
				(UUID) row[0],
				(String) row[1],
				(String) row[2],
				(LocalDateTime) row[3],
				now,
				"사용중"
			))
			.toList();
	}

	private List<LifecycleEventResponse> findIntangibleExpiringEvents(
		UUID companyId,
		LocalDateTime now,
		LocalDateTime limit
	) {
		return entityManager.createQuery("""
				select a.id, a.assetCode, i.productName, a.expiredAt
				from IntangibleAsset a
				join a.intangibleAssetItem i
				where a.company.id = :companyId
					and a.expiredAt between :now and :limit
					and a.intangibleAssetStatus not in :excludedStatuses
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("now", now)
			.setParameter("limit", limit)
			.setParameter("excludedStatuses", List.of(
				IntangibleAssetStatus.EXPIRED,
				IntangibleAssetStatus.CANCELLED
			))
			.getResultList()
			.stream()
			.map(row -> lifecycleEvent(
				"LICENSE_EXPIRING",
				INTANGIBLE,
				(UUID) row[0],
				(String) row[1],
				(String) row[2],
				(LocalDateTime) row[3],
				now,
				"라이선스만료예정"
			))
			.toList();
	}

	private List<LifecycleEventResponse> findIntangibleStatusEvents(UUID companyId, LocalDateTime now) {
		return entityManager.createQuery("""
				select a.id, a.assetCode, i.productName, coalesce(a.expiredAt, a.updatedAt), a.intangibleAssetStatus
				from IntangibleAsset a
				join a.intangibleAssetItem i
				where a.company.id = :companyId
					and (
						a.intangibleAssetStatus in :statuses
						or (a.expiredAt < :now and a.intangibleAssetStatus <> :cancelled)
					)
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("statuses", List.of(
				IntangibleAssetStatus.EXPIRED,
				IntangibleAssetStatus.CANCELLED
			))
			.setParameter("now", now)
			.setParameter("cancelled", IntangibleAssetStatus.CANCELLED)
			.getResultList()
			.stream()
			.map(row -> {
				IntangibleAssetStatus status = (IntangibleAssetStatus) row[4];
				boolean expiredByDate = ((LocalDateTime) row[3]).isBefore(now)
					&& status != IntangibleAssetStatus.CANCELLED;
				return lifecycleEvent(
					status == IntangibleAssetStatus.CANCELLED ? "LICENSE_CANCELLED" : "LICENSE_EXPIRED",
					INTANGIBLE,
					(UUID) row[0],
					(String) row[1],
					(String) row[2],
					(LocalDateTime) row[3],
					now,
					expiredByDate ? "만료" : toIntangibleLifecycleStatus(status)
				);
			})
			.toList();
	}

	private LifecycleEventResponse lifecycleEvent(
		String eventType,
		String assetType,
		UUID assetId,
		String assetCode,
		String assetName,
		LocalDateTime dueAt,
		LocalDateTime now,
		String status
	) {
		Long dDay = dueAt == null ? null : ChronoUnit.DAYS.between(now.toLocalDate(), dueAt.toLocalDate());
		return LifecycleEventResponse.builder()
			.eventType(eventType)
			.assetType(assetType)
			.assetId(assetId)
			.assetCode(assetCode)
			.assetName(assetName)
			.dueAt(dueAt)
			.dDay(dDay)
			.status(status)
			.build();
	}

	private String toTangibleLifecycleEventType(TangibleAssetStatus status) {
		return switch (status) {
			case RETURN_REQUESTED -> "RETURN_REQUESTED";
			case REPAIR_REQUESTED -> "REPAIR_REQUESTED";
			case REPAIRING -> "REPAIRING";
			case DISPOSED -> "DISPOSED";
			default -> "TANGIBLE_STATUS";
		};
	}

	private String toTangibleLifecycleStatus(TangibleAssetStatus status) {
		return switch (status) {
			case RETURN_REQUESTED -> "반납요청";
			case REPAIR_REQUESTED -> "수리요청";
			case REPAIRING -> "수리중";
			case DISPOSED -> "처분완료";
			default -> status.name();
		};
	}

	private String toIntangibleLifecycleStatus(IntangibleAssetStatus status) {
		return switch (status) {
			case EXPIRED -> "만료";
			case CANCELLED -> "해지완료";
			default -> status.name();
		};
	}

	private LifecycleEventResponse lifecycleEvent(
		String eventType,
		String assetType,
		UUID assetId,
		String assetCode,
		String assetName,
		LocalDateTime dueAt,
		LocalDateTime now
	) {
		long dDay = ChronoUnit.DAYS.between(now.toLocalDate(), dueAt.toLocalDate());
		return LifecycleEventResponse.builder()
			.eventType(eventType)
			.assetType(assetType)
			.assetId(assetId)
			.assetCode(assetCode)
			.assetName(assetName)
			.dueAt(dueAt)
			.dDay(dDay)
			.status(dDay < 0 ? "연체" : "예정")
			.build();
	}

	private BudgetLedgerResponse toBudgetLedgerResponse(BudgetHistory history) {
		BigDecimal balance = history.getTotalBudget()
			.subtract(history.getUsedAmountAfter())
			.subtract(history.getHoldAmountAfter());

		return BudgetLedgerResponse.builder()
			.historyId(history.getId())
			.date(history.getCreatedAt())
			.departmentId(history.getDepartment() == null ? null : history.getDepartment().getId())
			.departmentName(history.getDepartment() == null ? "전사공통" : history.getDepartment().getName())
			.type(toLedgerType(history.getHistoryType()))
			.historyType(history.getHistoryType())
			.usage(resolveUsage(history))
			.amount(toSignedAmount(history))
			.balance(balance)
			.ticketId(history.getTicket() == null ? null : history.getTicket().getId())
			.ticketNo(history.getTicket() == null ? null : history.getTicket().getTicketNo())
			.purchasePlanId(history.getPurchasePlan() == null ? null : history.getPurchasePlan().getId())
			.purchasePlanNo(history.getPurchasePlan() == null ? null : history.getPurchasePlan().getPlanNo())
			.build();
	}

	private String toLedgerType(BudgetHistoryType type) {
		return switch (type) {
			case HOLD_INCREASE -> "대기";
			case HOLD_DECREASE -> "해제";
			case USE_INCREASE -> "차감";
			case RECOVERY -> "회수";
			case TRANSFER -> "이관";
		};
	}

	private BigDecimal toSignedAmount(BudgetHistory history) {
		return switch (history.getHistoryType()) {
			case HOLD_INCREASE, RECOVERY -> history.getAmount();
			case HOLD_DECREASE, USE_INCREASE, TRANSFER -> history.getAmount().negate();
		};
	}

	private String resolveUsage(BudgetHistory history) {
		if (StringUtils.hasText(history.getDescription())) {
			return history.getDescription();
		}
		if (history.getTicket() != null) {
			return history.getTicket().getTicketNo();
		}
		if (history.getPurchasePlan() != null) {
			return history.getPurchasePlan().getPlanNo();
		}
		return "-";
	}

	private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
		if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return numerator
			.multiply(BigDecimal.valueOf(100))
			.divide(denominator, 1, RoundingMode.HALF_UP);
	}

	private String normalizeKeyword(String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return null;
		}
		return "%" + keyword.trim().toLowerCase() + "%";
	}

	private String toKeywordPattern(String keyword) {
		return keyword;
	}

	private Long calculateDashboardDayCount(LocalDateTime dueDate, LocalDateTime now) {
		if (dueDate == null) {
			return null;
		}
		return Math.abs(ChronoUnit.DAYS.between(now, dueDate));
	}

	private String resolveDashboardDayStatusLabel(LocalDateTime dueDate, LocalDateTime now) {
		if (dueDate == null) {
			return null;
		}
		return dueDate.isBefore(now) ? "OVERDUE" : "REMAINING";
	}

	private String resolveExpirationPeriodStatus(LocalDateTime expirationDate, LocalDateTime now) {
		if (expirationDate == null) {
			return null;
		}
		long days = ChronoUnit.DAYS.between(now.toLocalDate(), expirationDate.toLocalDate());
		if (days < 0) {
			return "EXPIRED";
		}
		if (days == 0) {
			return "EXPIRES_TODAY";
		}
		return "REMAINING";
	}

	private UUID getMemberDepartmentId(UUID companyId, UUID memberId) {
		return findMember(companyId, memberId).getDepartment().getId();
	}

	private Member findMember(UUID companyId, UUID memberId) {
		return memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
	}

	private <T> PaginationResponse<T> toPaginationResponse(List<T> content, PaginationRequest request) {
		int page = request.getPage();
		int size = request.getSize();
		int start = Math.min(page * size, content.size());
		int end = Math.min(start + size, content.size());
		Page<T> pageResult = new PageImpl<>(
			content.subList(start, end),
			request.toPageable(),
			content.size()
		);
		return PaginationResponse.from(pageResult);
	}

	private <T> PaginationResponse<T> toPaginationResponse(List<T> content, PaginationRequest request, long total) {
		Page<T> pageResult = new PageImpl<>(
			content,
			request.toPageable(),
			total
		);
		return PaginationResponse.from(pageResult);
	}
}
