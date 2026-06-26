package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"departmentId",
	"departmentName",
	"totalAmount",
	"usedAmount",
	"remainingAmount",
	"usageRate",
	"remainingRate"
})
public class EmployeeDepartmentBudgetResponse {

	private final UUID departmentId;
	private final String departmentName;
	private final BigDecimal totalAmount;
	private final BigDecimal usedAmount;
	private final BigDecimal remainingAmount;
	private final BigDecimal usageRate;
	private final BigDecimal remainingRate;
}

