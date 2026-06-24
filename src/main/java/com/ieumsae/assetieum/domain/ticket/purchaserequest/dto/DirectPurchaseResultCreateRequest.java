package com.ieumsae.assetieum.domain.ticket.purchaserequest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DirectPurchaseResultCreateRequest {

	@NotNull(message = "실제 결제 금액은 필수입니다.")
	@DecimalMin(value = "0.00", message = "실제 결제 금액은 0 이상이어야 합니다.")
	private BigDecimal actualPrice;

	@NotNull(message = "구매 일시는 필수입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime purchaseDate;

	@NotBlank(message = "구매처는 필수입니다.")
	@Size(max = 150, message = "구매처는 150자 이하여야 합니다.")
	private String purchaseVendor;

	@Size(max = 100, message = "시리얼번호는 100자 이하여야 합니다.")
	private String serialNumber;

	private List<@Size(max = 100) String> serialNumbers;

	@Size(max = 150, message = "위치는 150자 이하여야 합니다.")
	private String location;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime warrantyExpiredAt;

	@Size(max = 50, message = "라이선스코드는 50자 이하여야 합니다.")
	private String licenseCode;

	private List<@Size(max = 50) String> licenseCodes;

	private Integer seatCount;

	private Boolean isAutoRenewal;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime startedAt;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime expiredAt;

	private BillingCycle billingCycle;

}
