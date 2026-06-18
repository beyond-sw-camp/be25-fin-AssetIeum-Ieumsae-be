package com.ieumsae.assetieum.domain.ticket.assetrequest.dto;

import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import jakarta.validation.constraints.Min;
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
public class AssetRequestTicketCreateRequest {

	@NotNull(message = "요청 사용 유형은 필수입니다.")
	private RequestedUsageType requestedUsageType;

	@NotNull(message = "자산 유형은 필수입니다.")
	private AssetType assetType;

	@NotNull(message = "자산 품목 ID는 필수입니다.")
	private UUID assetItemId;

	@NotNull(message = "수량은 필수입니다.")
	@Min(value = 1, message = "수량은 1 이상이어야 합니다.")
	private Integer quantity;

	@NotBlank(message = "요청 사유는 필수입니다.")
	@Size(max = 255, message = "요청 사유는 255자 이하여야 합니다.")
	private String requestReason;
}
