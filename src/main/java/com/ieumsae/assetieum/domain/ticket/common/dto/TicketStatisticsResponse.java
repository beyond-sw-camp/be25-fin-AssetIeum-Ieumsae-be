package com.ieumsae.assetieum.domain.ticket.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketStatisticsResponse {

	private final long totalCount;

	private final long newOrPendingReviewCount;

	private final long inProgressCount;

	private final long completedCount;
}
