package com.ieumsae.assetieum.domain.ticket.purchasereturn.dto;

import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseReturnTicketCreateRequest {

	@NotNull(message = "자산 유형은 필수입니다.")
	private AssetReturnTargetType assetType;

	@NotNull(message = "자산 배정 ID는 필수입니다.")
	private UUID assignmentId;

	@NotBlank(message = "반품 사유는 필수입니다.")
	@Size(max = 255, message = "반품 사유는 255자 이하여야 합니다.")
	private String requestReason;
}
