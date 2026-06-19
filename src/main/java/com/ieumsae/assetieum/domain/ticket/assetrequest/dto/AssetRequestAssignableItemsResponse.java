package com.ieumsae.assetieum.domain.ticket.assetrequest.dto;

import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetRequestAssignableItemsResponse {

	private final AssetRequestAssignableItemResponse requestedItem;
	private final PaginationResponse<AssetRequestAssignableItemResponse> items;
}
