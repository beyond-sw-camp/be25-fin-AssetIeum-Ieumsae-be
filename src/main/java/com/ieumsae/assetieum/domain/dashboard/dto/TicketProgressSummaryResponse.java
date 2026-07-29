package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
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
