package com.ieumsae.assetieum.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"returnRequestCreatedCount",
	"returnRequestCreatedChangeRate",
	"returnCompletedCount",
	"returnCompletedChangeRate",
	"averageRecoveryDays",
	"averageRecoveryDaysChangeRate",
	"totalRecoveryDelayDays",
	"totalRecoveryDelayDaysChangeRate"
})
public class RecoveryOperationReportResponse {

	private final long returnRequestCreatedCount;
	private final BigDecimal returnRequestCreatedChangeRate;
	private final long returnCompletedCount;
	private final BigDecimal returnCompletedChangeRate;
	private final BigDecimal averageRecoveryDays;
	private final BigDecimal averageRecoveryDaysChangeRate;
	private final BigDecimal totalRecoveryDelayDays;
	private final BigDecimal totalRecoveryDelayDaysChangeRate;
}
