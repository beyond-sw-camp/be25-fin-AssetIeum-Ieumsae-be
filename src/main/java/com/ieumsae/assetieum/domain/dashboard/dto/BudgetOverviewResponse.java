package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"commonBudget",
	"departmentBudgets"
})
public class BudgetOverviewResponse {

	private final CommonBudgetSummary commonBudget;
	private final PaginationResponse<DepartmentBudgetSummary> departmentBudgets;

	@Getter
	@Builder
	@JsonPropertyOrder({
		"totalAmount",
		"remainingAmount",
		"remainingRate"
	})
	public static class CommonBudgetSummary {
		private final BigDecimal totalAmount;
		private final BigDecimal remainingAmount;
		private final BigDecimal remainingRate;
	}

	@Getter
	@Builder
	@JsonPropertyOrder({
		"departmentId",
		"departmentName",
		"totalAmount",
		"usedAmount",
		"usageRate"
	})
	public static class DepartmentBudgetSummary {
		private final UUID departmentId;
		private final String departmentName;
		private final BigDecimal totalAmount;
		private final BigDecimal usedAmount;
		private final BigDecimal usageRate;
	}
}
