package com.ieumsae.assetieum.domain.report.service;

import com.ieumsae.assetieum.global.common.util.KstDateTime;

import com.ieumsae.assetieum.domain.report.dto.OperationReportPageRequest;
import com.ieumsae.assetieum.domain.report.dto.OperationReportPeriodRequest;
import com.ieumsae.assetieum.domain.report.dto.PurchaseOperationReportResponse;
import com.ieumsae.assetieum.domain.report.dto.PurchaseOperationReportResponse.DepartmentPurchaseRequestSummary;
import com.ieumsae.assetieum.domain.report.dto.RecoveryOperationReportResponse;
import com.ieumsae.assetieum.domain.report.dto.UnreturnedAssetReportResponse;
import com.ieumsae.assetieum.domain.report.dto.UnreturnedAssetReportResponse.DepartmentUnreturnedAssetSummary;
import com.ieumsae.assetieum.domain.report.dto.UnreturnedAssetReportResponse.DelayedUserSummary;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.PurchaseRequestTicketStatus;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperationReportService {

	private static final int REPEAT_DELAY_THRESHOLD = 2;
	private static final int TOP_DELAYED_USER_LIMIT = 3;

	private final EntityManager entityManager;

	public UnreturnedAssetReportResponse getUnreturnedAssetReport(
		UUID companyId,
		Integer topDelayedUserLimit
	) {
		LocalDateTime now = KstDateTime.now();
		int topLimit = resolveTopDelayedUserLimit(topDelayedUserLimit);
		long totalUnreturnedAssetCount = countUnreturnedAssets(companyId);
		List<Object[]> overdueRows = findOverdueAssets(companyId, now);
		List<DepartmentUnreturnedAssetSummary> departmentSummaries =
			getDepartmentUnreturnedAssetSummaries(companyId, now);
		Map<UUID, DelayedUserAccumulator> delayedUsers = aggregateDelayedUsers(overdueRows, now);

		long totalUserCount = countActiveMembers(companyId);
		long repeatDelayedUserCount = delayedUsers.values().stream()
			.filter(user -> user.delayCount >= REPEAT_DELAY_THRESHOLD)
			.count();

		List<DelayedUserAccumulator> rankedDelayedUsers = delayedUsers.values().stream()
			.filter(user -> user.delayCount >= REPEAT_DELAY_THRESHOLD)
			.sorted(Comparator
				.comparingLong(DelayedUserAccumulator::delayCount).reversed()
				.thenComparing(DelayedUserAccumulator::averageDelayDays, Comparator.reverseOrder())
				.thenComparing(DelayedUserAccumulator::memberName))
			.limit(topLimit)
			.toList();
		List<DelayedUserSummary> topDelayedUsers = IntStream.range(0, rankedDelayedUsers.size())
			.mapToObj(index -> toDelayedUserSummary(rankedDelayedUsers.get(index), index + 1))
			.toList();

		return UnreturnedAssetReportResponse.builder()
			.totalUnreturnedAssetCount(totalUnreturnedAssetCount)
			.overdueReturnCount(overdueRows.size())
			.departmentUnreturnedAssets(departmentSummaries)
			.repeatDelayedUserCount(repeatDelayedUserCount)
			.repeatDelayedUserRate(percent(BigDecimal.valueOf(repeatDelayedUserCount), BigDecimal.valueOf(totalUserCount)))
			.topDelayedUsers(topDelayedUsers)
			.build();
	}

	private int resolveTopDelayedUserLimit(Integer topDelayedUserLimit) {
		if (topDelayedUserLimit == null) {
			return TOP_DELAYED_USER_LIMIT;
		}
		return Math.max(topDelayedUserLimit, 0);
	}

	public RecoveryOperationReportResponse getRecoveryOperationReport(
		UUID companyId,
		OperationReportPeriodRequest request
	) {
		Period current = resolvePeriod(request);
		Period previous = current.previous();

		long currentCreatedCount = countReturnRequestsCreated(companyId, current);
		long previousCreatedCount = countReturnRequestsCreated(companyId, previous);
		long currentCompletedCount = countReturnCompleted(companyId, current);
		long previousCompletedCount = countReturnCompleted(companyId, previous);
		RecoveryDuration currentDuration = calculateRecoveryDuration(companyId, current);
		RecoveryDuration previousDuration = calculateRecoveryDuration(companyId, previous);

		return RecoveryOperationReportResponse.builder()
			.returnRequestCreatedCount(currentCreatedCount)
			.returnRequestCreatedChangeRate(changeRate(currentCreatedCount, previousCreatedCount))
			.returnCompletedCount(currentCompletedCount)
			.returnCompletedChangeRate(changeRate(currentCompletedCount, previousCompletedCount))
			.averageRecoveryDays(currentDuration.averageDays())
			.averageRecoveryDaysChangeRate(changeRate(currentDuration.averageDays(), previousDuration.averageDays()))
			.totalRecoveryDelayDays(currentDuration.totalDays())
			.totalRecoveryDelayDaysChangeRate(changeRate(currentDuration.totalDays(), previousDuration.totalDays()))
			.build();
	}

	public PurchaseOperationReportResponse getPurchaseOperationReport(
		UUID companyId,
		OperationReportPageRequest request
	) {
		Period current = resolvePeriod(request);
		Period previous = current.previous();

		long currentPurchaseQuantity = sumPurchaseQuantity(companyId, current);
		long previousPurchaseQuantity = sumPurchaseQuantity(companyId, previous);
		List<DepartmentPurchaseRequestSummary> departmentSummaries = getDepartmentPurchaseSummaries(companyId, current);

		return PurchaseOperationReportResponse.builder()
			.newPurchaseQuantity(currentPurchaseQuantity)
			.newPurchaseQuantityChangeRate(changeRate(currentPurchaseQuantity, previousPurchaseQuantity))
			.departmentPurchaseRequests(toPaginationResponse(departmentSummaries, request))
			.build();
	}

	private long countUnreturnedAssets(UUID companyId) {
		return entityManager.createQuery("""
				select count(asset)
				from TangibleAsset asset
				where asset.company.id = :companyId
					and asset.tangibleAssetStatus in :statuses
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("statuses", unreturnedStatuses())
			.getSingleResult();
	}

	private List<TangibleAssetStatus> unreturnedStatuses() {
		return List.of(
			TangibleAssetStatus.IN_USE,
			TangibleAssetStatus.RETURN_REQUESTED,
			TangibleAssetStatus.REPAIR_REQUESTED,
			TangibleAssetStatus.REPAIRING
		);
	}

	private List<Object[]> findOverdueAssets(UUID companyId, LocalDateTime now) {
		return entityManager.createQuery("""
				select member.id, member.name, department.name, asset.returnDueDate
				from TangibleAsset asset
				join asset.member member
				join asset.department department
				where asset.company.id = :companyId
					and asset.tangibleAssetStatus = :status
					and asset.usageType = :usageType
					and asset.returnDueDate < :now
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.setParameter("usageType", UsageType.TEMPORARY)
			.setParameter("now", now)
			.getResultList();
	}

	private List<DepartmentUnreturnedAssetSummary> getDepartmentUnreturnedAssetSummaries(
		UUID companyId,
		LocalDateTime now
	) {
		List<Object[]> departments = entityManager.createQuery("""
				select department.id, department.name
				from Department department
				where department.company.id = :companyId
					and department.deletedAt is null
				order by department.name asc
				""", Object[].class)
			.setParameter("companyId", companyId)
			.getResultList();

		return departments.stream()
			.map(row -> {
				UUID departmentId = (UUID) row[0];
				String departmentName = (String) row[1];
				return DepartmentUnreturnedAssetSummary.builder()
					.departmentId(departmentId)
					.departmentName(departmentName)
					.unreturnedAssetCount(countDepartmentUnreturnedAssets(companyId, departmentId))
					.overdueReturnCount(countDepartmentOverdueAssets(companyId, departmentId, now))
					.build();
			})
			.toList();
	}

	private long countDepartmentUnreturnedAssets(UUID companyId, UUID departmentId) {
		return entityManager.createQuery("""
				select count(asset)
				from TangibleAsset asset
				where asset.company.id = :companyId
					and asset.department.id = :departmentId
					and asset.tangibleAssetStatus in :statuses
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("statuses", unreturnedStatuses())
			.getSingleResult();
	}

	private long countDepartmentOverdueAssets(UUID companyId, UUID departmentId, LocalDateTime now) {
		return entityManager.createQuery("""
				select count(asset)
				from TangibleAsset asset
				where asset.company.id = :companyId
					and asset.department.id = :departmentId
					and asset.tangibleAssetStatus = :status
					and asset.usageType = :usageType
					and asset.returnDueDate < :now
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("status", TangibleAssetStatus.IN_USE)
			.setParameter("usageType", UsageType.TEMPORARY)
			.setParameter("now", now)
			.getSingleResult();
	}

	private Map<UUID, DelayedUserAccumulator> aggregateDelayedUsers(List<Object[]> rows, LocalDateTime now) {
		Map<UUID, DelayedUserAccumulator> result = new LinkedHashMap<>();
		for (Object[] row : rows) {
			UUID memberId = (UUID) row[0];
			String memberName = (String) row[1];
			String departmentName = (String) row[2];
			LocalDateTime returnDueDate = (LocalDateTime) row[3];
			long delayDays = Math.max(ChronoUnit.DAYS.between(returnDueDate.toLocalDate(), now.toLocalDate()), 0);

			result.computeIfAbsent(memberId, id -> new DelayedUserAccumulator(
				memberId,
				memberName,
				departmentName
			)).addDelay(delayDays, returnDueDate);
		}
		return result;
	}

	private DelayedUserSummary toDelayedUserSummary(DelayedUserAccumulator user, int rank) {
		return DelayedUserSummary.builder()
			.rank(rank)
			.memberId(user.memberId)
			.memberName(user.memberName)
			.departmentName(user.departmentName)
			.delayCount(user.delayCount)
			.averageDelayDays(user.averageDelayDays())
			.recentDelayedAt(user.recentDelayedAt)
			.build();
	}

	private long countActiveMembers(UUID companyId) {
		return entityManager.createQuery("""
				select count(member)
				from Member member
				where member.company.id = :companyId
					and member.deletedAt is null
				""", Long.class)
			.setParameter("companyId", companyId)
			.getSingleResult();
	}

	private long countReturnRequestsCreated(UUID companyId, Period period) {
		return entityManager.createQuery("""
				select count(returnTicket)
				from AssetReturnTicket returnTicket
				where returnTicket.company.id = :companyId
					and returnTicket.ticket.createdAt >= :startDate
					and returnTicket.ticket.createdAt < :endDate
					and returnTicket.deletedAt is null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("startDate", period.startDate())
			.setParameter("endDate", period.endDate())
			.getSingleResult();
	}

	private long countReturnCompleted(UUID companyId, Period period) {
		return entityManager.createQuery("""
				select count(returnTicket)
				from AssetReturnTicket returnTicket
				where returnTicket.company.id = :companyId
					and returnTicket.status = :status
					and returnTicket.processedAt >= :startDate
					and returnTicket.processedAt < :endDate
					and returnTicket.deletedAt is null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("status", AssetReturnTicketStatus.COMPLETED)
			.setParameter("startDate", period.startDate())
			.setParameter("endDate", period.endDate())
			.getSingleResult();
	}

	private RecoveryDuration calculateRecoveryDuration(UUID companyId, Period period) {
		List<Object[]> rows = entityManager.createQuery("""
				select returnTicket.ticket.createdAt, returnTicket.processedAt
				from AssetReturnTicket returnTicket
				where returnTicket.company.id = :companyId
					and returnTicket.status = :status
					and returnTicket.processedAt >= :startDate
					and returnTicket.processedAt < :endDate
					and returnTicket.deletedAt is null
					and returnTicket.processedAt is not null
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("status", AssetReturnTicketStatus.COMPLETED)
			.setParameter("startDate", period.startDate())
			.setParameter("endDate", period.endDate())
			.getResultList();

		if (rows.isEmpty()) {
			return new RecoveryDuration(BigDecimal.ZERO, BigDecimal.ZERO);
		}

		BigDecimal totalDays = rows.stream()
			.map(row -> BigDecimal.valueOf(Duration.between((LocalDateTime) row[0], (LocalDateTime) row[1]).toHours())
				.divide(BigDecimal.valueOf(24), 1, RoundingMode.HALF_UP))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		return new RecoveryDuration(
			totalDays.divide(BigDecimal.valueOf(rows.size()), 1, RoundingMode.HALF_UP),
			totalDays
		);
	}

	private long sumPurchaseQuantity(UUID companyId, Period period) {
		Long result = entityManager.createQuery("""
				select coalesce(sum(request.quantity), 0)
				from PurchaseRequestTicket request
				where request.company.id = :companyId
					and request.ticket.createdAt >= :startDate
					and request.ticket.createdAt < :endDate
					and request.deletedAt is null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("startDate", period.startDate())
			.setParameter("endDate", period.endDate())
			.getSingleResult();
		return result == null ? 0 : result;
	}

	private List<DepartmentPurchaseRequestSummary> getDepartmentPurchaseSummaries(UUID companyId, Period period) {
		List<Object[]> departments = entityManager.createQuery("""
				select department.id, department.name
				from Department department
				where department.company.id = :companyId
					and department.deletedAt is null
				order by department.name asc
				""", Object[].class)
			.setParameter("companyId", companyId)
			.getResultList();

		List<DepartmentPurchaseRequestSummary> summaries = new ArrayList<>();
		for (Object[] department : departments) {
			UUID departmentId = (UUID) department[0];
			String departmentName = (String) department[1];
			summaries.add(DepartmentPurchaseRequestSummary.builder()
				.departmentId(departmentId)
				.departmentName(departmentName)
				.purchaseRequestCount(countDepartmentPurchaseRequests(companyId, departmentId, period, null))
				.purchaseApprovedCount(countDepartmentApprovedPurchaseRequests(companyId, departmentId, period))
				.purchaseCompletedCount(countDepartmentPurchaseRequests(companyId, departmentId, period, PurchaseRequestTicketStatus.COMPLETED))
				.accumulatedPurchaseQuantity(sumDepartmentPurchaseQuantity(companyId, departmentId, period))
				.build());
		}
		return summaries;
	}

	private long countDepartmentPurchaseRequests(
		UUID companyId,
		UUID departmentId,
		Period period,
		PurchaseRequestTicketStatus status
	) {
		return entityManager.createQuery("""
				select count(request)
				from PurchaseRequestTicket request
				where request.company.id = :companyId
					and request.ticket.department.id = :departmentId
					and request.ticket.createdAt >= :startDate
					and request.ticket.createdAt < :endDate
					and (:status is null or request.status = :status)
					and request.deletedAt is null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("startDate", period.startDate())
			.setParameter("endDate", period.endDate())
			.setParameter("status", status)
			.getSingleResult();
	}

	private long countDepartmentApprovedPurchaseRequests(UUID companyId, UUID departmentId, Period period) {
		return entityManager.createQuery("""
				select count(request)
				from PurchaseRequestTicket request
				where request.company.id = :companyId
					and request.ticket.department.id = :departmentId
					and request.ticket.purchaseApprovedAt >= :startDate
					and request.ticket.purchaseApprovedAt < :endDate
					and request.ticket.ticketStatus in :statuses
					and request.deletedAt is null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("startDate", period.startDate())
			.setParameter("endDate", period.endDate())
			.setParameter("statuses", List.of(
				TicketStatus.ASSET_APPROVED,
				TicketStatus.IN_PROGRESS,
				TicketStatus.COMPLETED
			))
			.getSingleResult();
	}

	private long sumDepartmentPurchaseQuantity(UUID companyId, UUID departmentId, Period period) {
		Long result = entityManager.createQuery("""
				select coalesce(sum(request.quantity), 0)
				from PurchaseRequestTicket request
				where request.company.id = :companyId
					and request.ticket.department.id = :departmentId
					and request.ticket.createdAt >= :startDate
					and request.ticket.createdAt < :endDate
					and request.deletedAt is null
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("startDate", period.startDate())
			.setParameter("endDate", period.endDate())
			.getSingleResult();
		return result == null ? 0 : result;
	}

	private Period resolvePeriod(OperationReportPeriodRequest request) {
		return resolvePeriod(request.getStartDate(), request.getEndDate());
	}

	private Period resolvePeriod(OperationReportPageRequest request) {
		return resolvePeriod(request.getStartDate(), request.getEndDate());
	}

	private Period resolvePeriod(LocalDateTime requestStartDate, LocalDateTime requestEndDate) {
		LocalDateTime endDate = requestEndDate == null ? KstDateTime.now() : requestEndDate;
		LocalDateTime startDate = requestStartDate == null ? endDate.minusDays(30) : requestStartDate;
		return new Period(startDate, endDate);
	}

	private BigDecimal changeRate(long current, long previous) {
		return changeRate(BigDecimal.valueOf(current), BigDecimal.valueOf(previous));
	}

	private BigDecimal changeRate(BigDecimal current, BigDecimal previous) {
		if (previous.compareTo(BigDecimal.ZERO) == 0) {
			return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
		}
		return current.subtract(previous)
			.multiply(BigDecimal.valueOf(100))
			.divide(previous, 1, RoundingMode.HALF_UP);
	}

	private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
		if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return numerator.multiply(BigDecimal.valueOf(100))
			.divide(denominator, 1, RoundingMode.HALF_UP);
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

	private record Period(LocalDateTime startDate, LocalDateTime endDate) {
		private Period previous() {
			long seconds = Duration.between(startDate, endDate).getSeconds();
			return new Period(startDate.minusSeconds(seconds), startDate);
		}
	}

	private record RecoveryDuration(BigDecimal averageDays, BigDecimal totalDays) {
	}

	private static class DelayedUserAccumulator {
		private final UUID memberId;
		private final String memberName;
		private final String departmentName;
		private long delayCount;
		private long totalDelayDays;
		private LocalDateTime recentDelayedAt;

		private DelayedUserAccumulator(UUID memberId, String memberName, String departmentName) {
			this.memberId = memberId;
			this.memberName = memberName;
			this.departmentName = departmentName;
		}

		private void addDelay(long delayDays, LocalDateTime delayedAt) {
			this.delayCount++;
			this.totalDelayDays += delayDays;
			if (recentDelayedAt == null || delayedAt.isAfter(recentDelayedAt)) {
				this.recentDelayedAt = delayedAt;
			}
		}

		private long delayCount() {
			return delayCount;
		}

		private String memberName() {
			return memberName;
		}

		private BigDecimal averageDelayDays() {
			if (delayCount == 0) {
				return BigDecimal.ZERO;
			}
			return BigDecimal.valueOf(totalDelayDays)
				.divide(BigDecimal.valueOf(delayCount), 1, RoundingMode.HALF_UP);
		}
	}
}
