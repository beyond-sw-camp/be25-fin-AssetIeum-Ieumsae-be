package com.ieumsae.assetieum.domain.tangibleasset.item.dto;

import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailableRentalItemResponse {

	private final UUID tangibleAssetItemId;
	private final UUID categoryId;
	private final String categoryName;
	private final String productName;
	private final String manufacturer;
	private final String modelName;
	private final Boolean isStandard;
	private final long availableAssetCount;

	public static AvailableRentalItemResponse from(
		TangibleAssetItem item,
		long availableAssetCount
	) {
		return AvailableRentalItemResponse.builder()
			.tangibleAssetItemId(item.getId())
			.categoryId(item.getTangibleAssetCategory().getId())
			.categoryName(item.getTangibleAssetCategory().getName())
			.productName(item.getProductName())
			.manufacturer(item.getManufacturer())
			.modelName(item.getModelName())
			.isStandard(item.getIsStandard())
			.availableAssetCount(availableAssetCount)
			.build();
	}
}
