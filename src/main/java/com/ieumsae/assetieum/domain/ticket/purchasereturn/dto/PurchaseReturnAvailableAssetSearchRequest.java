package com.ieumsae.assetieum.domain.ticket.purchasereturn.dto;

import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseReturnAvailableAssetSearchRequest {

	@NotNull(message = "자산 유형은 필수입니다.")
	private AssetReturnTargetType assetType;
}
