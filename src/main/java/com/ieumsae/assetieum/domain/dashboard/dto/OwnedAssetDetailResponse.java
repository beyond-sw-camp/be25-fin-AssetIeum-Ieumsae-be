package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnedAssetDetailResponse {

	private final AssetType assetType;
	private final UUID assetId;
	private final String assetName;
	private final String categoryName;
	private final String assetCode;
	private final Integer seatCount;
	private final Integer availableSeatCount;
	private final UUID departmentId;
	private final String departmentName;
	private final UUID renterId;
	private final String renterName;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime dueDate;
	private final Long dayCount;
	private final Long overdueDays;
}
