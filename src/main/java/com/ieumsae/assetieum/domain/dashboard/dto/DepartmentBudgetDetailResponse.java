package com.ieumsae.assetieum.domain.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentBudgetDetailResponse {
	private String departmentName;
	private BigDecimal totalAmount;
	private BigDecimal usedAmount;
	private BigDecimal remainingAmount;
	private BigDecimal usageRate;
	
	private List<BudgetCategoryUsage> categoryUsages;

	@Getter
	@Builder
	public static class BudgetCategoryUsage {
		private String categoryName;
		private BigDecimal amount;
		private BigDecimal percentage;
	}
}
