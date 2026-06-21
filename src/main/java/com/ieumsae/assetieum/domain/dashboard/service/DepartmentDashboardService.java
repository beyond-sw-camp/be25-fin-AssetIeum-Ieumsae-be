package com.ieumsae.assetieum.domain.dashboard.service;

import com.ieumsae.assetieum.domain.budget.history.type.BudgetHistoryType;
import com.ieumsae.assetieum.domain.dashboard.dto.DepartmentBudgetDetailResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.DepartmentBudgetDetailResponse.BudgetCategoryUsage;
import com.ieumsae.assetieum.domain.dashboard.dto.HrEventStatisticsResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.HrLifecycleEventResponse;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventStatus;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentDashboardService {

	private final EntityManager entityManager;

	public DepartmentBudgetDetailResponse getDepartmentBudgetDetails(UUID companyId, UUID departmentId, Integer year) {
		int targetYear = year == null ? LocalDateTime.now().getYear() : year;

		Object[] budgetOverview = (Object[]) entityManager.createQuery("""
				select b.department.name, b.totalAmount, b.usedAmount, (b.totalAmount - b.usedAmount - b.heldAmount)
				from Budget b
				where b.company.id = :companyId
					and b.department.id = :departmentId
					and b.budgetYear = :year
				""")
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("year", targetYear)
			.getResultStream()
			.findFirst()
			.orElse(new Object[]{"부서없음", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});

		String departmentName = (String) budgetOverview[0];
		BigDecimal totalAmount = (BigDecimal) budgetOverview[1];
		BigDecimal usedAmount = (BigDecimal) budgetOverview[2];
		BigDecimal remainingAmount = (BigDecimal) budgetOverview[3];

		List<Object[]> categoryResults = entityManager.createQuery("""
				select coalesce(b.description, '기타'), sum(b.amount)
				from BudgetHistory b
				where b.company.id = :companyId
					and b.department.id = :departmentId
					and b.budget.budgetYear = :year
					and b.historyType = :useType
				group by coalesce(b.description, '기타')
				order by sum(b.amount) desc
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("year", targetYear)
			.setParameter("useType", BudgetHistoryType.USE_INCREASE)
			.getResultList();

		List<BudgetCategoryUsage> categoryUsages = categoryResults.stream()
			.map(row -> {
				String category = (String) row[0];
				BigDecimal amount = (BigDecimal) row[1];
				return BudgetCategoryUsage.builder()
					.categoryName(category)
					.amount(amount)
					.percentage(percent(amount, usedAmount))
					.build();
			})
			.collect(Collectors.toList());

		return DepartmentBudgetDetailResponse.builder()
			.departmentName(departmentName)
			.totalAmount(totalAmount)
			.usedAmount(usedAmount)
			.remainingAmount(remainingAmount)
			.usageRate(percent(usedAmount, totalAmount))
			.categoryUsages(categoryUsages)
			.build();
	}

	public PaginationResponse<HrLifecycleEventResponse> getHrLifecycleEvents(
		UUID companyId,
		UUID departmentId,
		String eventTypeStr,
		PaginationRequest request
	) {
		HrEventType eventType = null;
		if (StringUtils.hasText(eventTypeStr) && !"ALL".equals(eventTypeStr)) {
			eventType = HrEventType.valueOf(eventTypeStr);
		}

		List<Object[]> rows = entityManager.createQuery("""
				select h.id, h.eventType, m.name, d.name, h.eventDate, h.hrEventStatus
				from HrEvent h
				join h.member m
				join h.department d
				where h.company.id = :companyId
					and (:departmentId is null or h.department.id = :departmentId)
					and (:eventType is null or h.eventType = :eventType)
				order by h.eventDate asc
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("eventType", eventType)
			.setFirstResult((int) request.toPageable().getOffset())
			.setMaxResults(request.toPageable().getPageSize())
			.getResultList();

		Long total = entityManager.createQuery("""
				select count(h)
				from HrEvent h
				where h.company.id = :companyId
					and (:departmentId is null or h.department.id = :departmentId)
					and (:eventType is null or h.eventType = :eventType)
				""", Long.class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.setParameter("eventType", eventType)
			.getSingleResult();

		LocalDateTime now = LocalDateTime.now();
		List<HrLifecycleEventResponse> content = rows.stream()
			.map(row -> {
				UUID id = (UUID) row[0];
				HrEventType type = (HrEventType) row[1];
				String memberName = (String) row[2];
				String departmentName = (String) row[3];
				LocalDateTime eventDate = (LocalDateTime) row[4];
				HrEventStatus status = (HrEventStatus) row[5];
				long dDay = eventDate != null
					? ChronoUnit.DAYS.between(now.toLocalDate(), eventDate.toLocalDate())
					: 0;

				return HrLifecycleEventResponse.builder()
					.eventId(id)
					.eventType(toHrEventTypeLabel(type))
					.memberName(memberName)
					.departmentName(departmentName)
					.eventDate(eventDate)
					.dDay(dDay)
					.status(toHrEventStatusLabel(status))
					.build();
			})
			.collect(Collectors.toList());

		Page<HrLifecycleEventResponse> page = new PageImpl<>(content, request.toPageable(), total);
		return PaginationResponse.from(page);
	}

	public HrEventStatisticsResponse getHrEventStatistics(UUID companyId, UUID departmentId) {
		List<Object[]> stats = entityManager.createQuery("""
				select h.hrEventStatus, count(h)
				from HrEvent h
				where h.company.id = :companyId
					and (:departmentId is null or h.department.id = :departmentId)
				group by h.hrEventStatus
				""", Object[].class)
			.setParameter("companyId", companyId)
			.setParameter("departmentId", departmentId)
			.getResultList();

		long pendingCount = 0;
		long inProgressCount = 0;
		long completedCount = 0;
		long cancelledCount = 0;

		for (Object[] row : stats) {
			HrEventStatus status = (HrEventStatus) row[0];
			long count = (long) row[1];
			switch (status) {
				case PENDING -> pendingCount = count;
				case IN_PROGRESS -> inProgressCount = count;
				case COMPLETED -> completedCount = count;
				case CANCELLED -> cancelledCount = count;
			}
		}

		long total = pendingCount + inProgressCount + completedCount + cancelledCount;
		return HrEventStatisticsResponse.builder()
			.totalCount(total)
			.pendingCount(pendingCount)
			.pendingPercentage(percent(BigDecimal.valueOf(pendingCount), BigDecimal.valueOf(total)))
			.inProgressCount(inProgressCount)
			.inProgressPercentage(percent(BigDecimal.valueOf(inProgressCount), BigDecimal.valueOf(total)))
			.completedCount(completedCount)
			.completedPercentage(percent(BigDecimal.valueOf(completedCount), BigDecimal.valueOf(total)))
			.cancelledCount(cancelledCount)
			.cancelledPercentage(percent(BigDecimal.valueOf(cancelledCount), BigDecimal.valueOf(total)))
			.build();
	}

	private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
		if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return numerator
			.multiply(BigDecimal.valueOf(100))
			.divide(denominator, 1, RoundingMode.HALF_UP);
	}

	private String toHrEventTypeLabel(HrEventType type) {
		return switch (type) {
			case ONBOARDING -> "입사";
			case OFFBOARDING -> "퇴사";
			case DEPARTMENT_TRANSFER -> "부서이동";
			case LEAVE -> "휴직";
			case RETURN -> "복직";
		};
	}

	private String toHrEventStatusLabel(HrEventStatus status) {
		return switch (status) {
			case PENDING -> "대기중";
			case IN_PROGRESS -> "진행중";
			case COMPLETED -> "완료";
			case CANCELLED -> "취소";
		};
	}
}
