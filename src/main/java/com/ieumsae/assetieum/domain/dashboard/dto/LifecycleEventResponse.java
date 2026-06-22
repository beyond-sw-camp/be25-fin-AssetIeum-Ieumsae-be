package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"eventType",
	"assetType",
	"assetId",
	"assetCode",
	"assetName",
	"dueAt",
	"dDay",
	"status"
})
public class LifecycleEventResponse {

	private final String eventType;
	private final String assetType;
	private final UUID assetId;
	private final String assetCode;
	private final String assetName;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime dueAt;

	private final Long dDay;
	private final String status;
}
