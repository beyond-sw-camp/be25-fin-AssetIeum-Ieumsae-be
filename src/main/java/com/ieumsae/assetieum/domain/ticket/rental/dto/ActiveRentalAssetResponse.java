package com.ieumsae.assetieum.domain.ticket.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActiveRentalAssetResponse {

	private final UUID assignmentId;
	private final UUID assetId;
	private final String assetCode;
	private final UUID tangibleAssetItemId;
	private final UUID categoryId;
	private final String categoryName;
	private final String productName;
	private final String manufacturer;
	private final String modelName;
	private final String serialNumber;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime assignedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime currentReturnDueDate;

	public static ActiveRentalAssetResponse from(TangibleAssetAssignment assignment) {
		TangibleAsset asset = assignment.getTangibleAsset();
		TangibleAssetItem item = asset.getTangibleAssetItem();

		return ActiveRentalAssetResponse.builder()
			.assignmentId(assignment.getId())
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.tangibleAssetItemId(item.getId())
			.categoryId(item.getTangibleAssetCategory().getId())
			.categoryName(item.getTangibleAssetCategory().getName())
			.productName(item.getProductName())
			.manufacturer(item.getManufacturer())
			.modelName(item.getModelName())
			.serialNumber(asset.getSerialNumber())
			.assignedAt(assignment.getAssignedAt())
			.currentReturnDueDate(asset.getReturnDueDate())
			.build();
	}
}
