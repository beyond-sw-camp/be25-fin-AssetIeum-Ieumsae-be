package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"assetType",
	"itemId",
	"assetName",
	"expectedDemand",
	"currentInventory",
	"scheduledReturn",
	"availabilityRate",
	"status"
})
public class AssetDemandResponse {

	private final String assetType;
	private final UUID itemId;
	private final String assetName;
	private final long expectedDemand;
	private final long currentInventory;
	private final long scheduledReturn;
	private final BigDecimal availabilityRate;
	private final String status;
}
