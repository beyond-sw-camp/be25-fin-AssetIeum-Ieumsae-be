package com.ieumsae.assetieum.domain.ticket.rental.dto;

import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RentalAssignableAssetResponse {

	private final UUID assetId;
	private final String assetCode;
	private final String serialNumber;
	private final TangibleAssetStatus status;
	private final String location;
	private final boolean reservedAsset;

	public static RentalAssignableAssetResponse from(TangibleAsset asset, UUID reservedAssetId) {
		return RentalAssignableAssetResponse.builder()
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.serialNumber(asset.getSerialNumber())
			.status(asset.getTangibleAssetStatus())
			.location(asset.getLocation())
			.reservedAsset(reservedAssetId != null && reservedAssetId.equals(asset.getId()))
			.build();
	}
}
