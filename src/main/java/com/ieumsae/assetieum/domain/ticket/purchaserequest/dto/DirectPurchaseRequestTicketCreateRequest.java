package com.ieumsae.assetieum.domain.ticket.purchaserequest.dto;

import com.ieumsae.assetieum.domain.intangibleasset.item.type.LicenseType;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DirectPurchaseRequestTicketCreateRequest {

	@NotNull(message = "요청 사용 유형은 필수입니다.")
	private RequestedUsageType requestedUsageType;

	@NotNull(message = "자산 유형은 필수입니다.")
	private AssetType assetType;

	@NotNull(message = "자산 카테고리 ID는 필수입니다.")
	private UUID categoryId;

	@NotBlank(message = "요청 품목 상세는 필수입니다.")
	@Size(max = 500, message = "요청 품목 상세는 500자 이하여야 합니다.")
	private String requestedItemDetail;

	@Size(max = 100, message = "제조사는 100자 이하여야 합니다.")
	private String manufacturer;

	private LicenseType licenseType;

	@Min(value = 1, message = "수량은 1 이상이어야 합니다.")
	private int quantity = 1;

	@DecimalMin(value = "0.00", message = "예상 금액은 0 이상이어야 합니다.")
	private BigDecimal expectedPrice;

	@Size(max = 255, message = "신청 사유는 255자 이하여야 합니다.")
	private String requestReason;
}
