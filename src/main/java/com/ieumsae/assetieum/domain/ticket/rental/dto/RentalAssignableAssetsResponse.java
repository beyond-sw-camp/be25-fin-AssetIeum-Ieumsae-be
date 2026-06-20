package com.ieumsae.assetieum.domain.ticket.rental.dto;

import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RentalAssignableAssetsResponse {

	private final RentalTicketDetailResponse.ItemSummary requestedItem;
	private final RentalAssignableAssetResponse reservedAsset;
	private final PaginationResponse<RentalAssignableAssetResponse> assets;
}
