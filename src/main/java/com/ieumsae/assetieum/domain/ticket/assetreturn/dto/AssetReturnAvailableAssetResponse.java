package com.ieumsae.assetieum.domain.ticket.assetreturn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetReturnAvailableAssetResponse {

	private final AssetReturnTargetType assetType;
	private final UUID assignmentId;
	private final UUID assetId;
	private final String assetCode;
	private final UUID itemId;
	private final UUID categoryId;
	private final String categoryName;
	private final String productName;
	private final String manufacturer;
	private final String modelName;
	private final String provider;
	private final String serialNumber;
	private final String licenseCode;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime assignedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime returnDueDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime expiredAt;

	public static AssetReturnAvailableAssetResponse from(TangibleAssetAssignment assignment) {
		TangibleAsset asset = assignment.getTangibleAsset();
		TangibleAssetItem item = asset.getTangibleAssetItem();

		return AssetReturnAvailableAssetResponse.builder()
			.assetType(AssetReturnTargetType.TANGIBLE)
			.assignmentId(assignment.getId())
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.itemId(item.getId())
			.categoryId(item.getTangibleAssetCategory().getId())
			.categoryName(item.getTangibleAssetCategory().getName())
			.productName(item.getProductName())
			.manufacturer(item.getManufacturer())
			.modelName(item.getModelName())
			.serialNumber(asset.getSerialNumber())
			.assignedAt(assignment.getAssignedAt())
			.returnDueDate(asset.getReturnDueDate())
			.build();
	}

	public static AssetReturnAvailableAssetResponse from(IntangibleAssetAssignment assignment) {
		IntangibleAsset asset = assignment.getIntangibleAsset();
		IntangibleAssetItem item = asset.getIntangibleAssetItem();

		return AssetReturnAvailableAssetResponse.builder()
			.assetType(AssetReturnTargetType.INTANGIBLE)
			.assignmentId(assignment.getId())
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.itemId(item.getId())
			.categoryId(item.getIntangibleAssetCategory().getId())
			.categoryName(item.getIntangibleAssetCategory().getName())
			.productName(item.getProductName())
			.provider(item.getProvider())
			.licenseCode(asset.getLicenseCode())
			.assignedAt(assignment.getAssignedAt())
			.expiredAt(asset.getExpiredAt())
			.build();
	}
}
