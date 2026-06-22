package com.ieumsae.assetieum.domain.dashboard.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HrEventStatisticsResponse {
	private long totalCount;
	private long pendingCount;
	private BigDecimal pendingPercentage;
	private long inProgressCount;
	private BigDecimal inProgressPercentage;
	private long completedCount;
	private BigDecimal completedPercentage;
	private long cancelledCount;
	private BigDecimal cancelledPercentage;
}
