package com.ieumsae.assetieum.domain.ticket.assetrequest.dto;

import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetRequestAssignableItemResponse {

	private final AssetType assetType;
	private final UUID itemId;
	private final UUID categoryId;
	private final String categoryName;
	private final String productName;
	private final String itemIdentifier;
	private final String manufacturerOrProvider;
	private final String modelName;
	private final String licenseType;
	private final Boolean isStandard;
	private final boolean requestedItem;
	private final int availableCount;

	public static AssetRequestAssignableItemResponse from(
		TangibleAssetItem item,
		UUID requestedItemId,
		int availableCount
	) {
		return AssetRequestAssignableItemResponse.builder()
			.assetType(AssetType.TANGIBLE)
			.itemId(item.getId())
			.categoryId(item.getTangibleAssetCategory().getId())
			.categoryName(item.getTangibleAssetCategory().getName())
			.productName(item.getProductName())
			.itemIdentifier(item.getId().toString())
			.manufacturerOrProvider(item.getManufacturer())
			.modelName(item.getModelName())
			.licenseType(null)
			.isStandard(item.getIsStandard())
			.requestedItem(item.getId().equals(requestedItemId))
			.availableCount(availableCount)
			.build();
	}

	public static AssetRequestAssignableItemResponse from(
		IntangibleAssetItem item,
		UUID requestedItemId,
		int availableCount
	) {
		return AssetRequestAssignableItemResponse.builder()
			.assetType(AssetType.INTANGIBLE)
			.itemId(item.getId())
			.categoryId(item.getIntangibleAssetCategory().getId())
			.categoryName(item.getIntangibleAssetCategory().getName())
			.productName(item.getProductName())
			.itemIdentifier(item.getId().toString())
			.manufacturerOrProvider(item.getProvider())
			.modelName(null)
			.licenseType(item.getLicenseType().name())
			.isStandard(item.getIsStandard())
			.requestedItem(item.getId().equals(requestedItemId))
			.availableCount(availableCount)
			.build();
	}
}
