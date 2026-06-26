package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"rentalScheduled",
	"rented",
	"overdue"
})
public class RentalAssetSummaryResponse {

	private final long rentalScheduled;
	private final long rented;
	private final long overdue;
}

