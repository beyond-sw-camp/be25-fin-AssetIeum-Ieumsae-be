package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"tangibleAssetCount",
	"intangibleAssetCount"
})
public class ExpiringAssetSummaryResponse {

	private final long tangibleAssetCount;
	private final long intangibleAssetCount;
}
