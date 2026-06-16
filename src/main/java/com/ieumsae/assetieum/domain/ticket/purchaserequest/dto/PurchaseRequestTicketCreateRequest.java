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
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseRequestTicketCreateRequest {

	@NotNull(message = "요청 사용 유형은 필수입니다.")
	private RequestedUsageType requestedUsageType;

	@NotNull(message = "자산 유형은 필수입니다.")
	private AssetType assetType;

	@NotNull(message = "자산 카테고리 ID는 필수입니다.")
	private UUID categoryId;

	@NotBlank(message = "요청 품목 상세는 필수입니다.")
	@Size(max = 500, message = "요청 품목 상세는 500자 이하여야 합니다.")
	private String requestedItemDetail;

	@NotBlank(message = "제조사는 필수입니다.")
	@Size(max = 100, message = "제조사는 100자 이하여야 합니다.")
	private String manufacturer;

	private LicenseType licenseType;

	@NotBlank(message = "구매 URL은 필수입니다.")
	@URL(message = "구매 URL 형식이 올바르지 않습니다.")
	@Size(max = 500, message = "구매 URL은 500자 이하여야 합니다.")
	private String purchaseUrl;

	@NotNull(message = "수량은 필수입니다.")
	@Min(value = 1, message = "수량은 1 이상이어야 합니다.")
	private Integer quantity;

	@NotNull(message = "예상 금액은 필수입니다.")
	@DecimalMin(value = "0.00", message = "예상 금액은 0 이상이어야 합니다.")
	private BigDecimal expectedPrice;

	@NotBlank(message = "신청 사유는 필수입니다.")
	@Size(max = 255, message = "신청 사유는 255자 이하여야 합니다.")
	private String requestReason;
}
