package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnedAssetDetailResponse {

	private final UUID assetId;
	private final String assetName;
	private final String categoryName;
	private final String assetCode;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime warrantyExpiredAt;
	private final UUID departmentId;
	private final String departmentName;
	private final UUID renterId;
	private final String renterName;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime usedStartedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime returnDueDate;
	private final Long overdueDays;
}
