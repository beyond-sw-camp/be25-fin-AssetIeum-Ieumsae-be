package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"waitingReceipt",
	"receiptCompleted",
	"processing",
	"completed"
})
public class TicketProgressSummaryResponse {

	private final long waitingReceipt;
	private final long receiptCompleted;
	private final long processing;
	private final long completed;
}
