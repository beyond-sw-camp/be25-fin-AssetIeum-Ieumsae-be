package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExpiringAssetDetailResponse {

	private final AssetType assetType;
	private final UUID assetId;
	private final String assetName;
	private final long remainingDays;
	private final UUID departmentId;
	private final String departmentName;
	private final UUID userId;
	private final String userName;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime expiredAt;
	private final String assetCode;
	private final String manufacturer;
	private final String issuer;
}
