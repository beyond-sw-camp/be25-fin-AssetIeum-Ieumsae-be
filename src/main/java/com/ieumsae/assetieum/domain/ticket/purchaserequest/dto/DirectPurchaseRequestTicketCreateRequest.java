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

	@NotNull(message = "Requested usage type is required.")
	private RequestedUsageType requestedUsageType;

	@NotNull(message = "Asset type is required.")
	private AssetType assetType;

	@NotNull(message = "Standard flag is required.")
	private Boolean isStandard;

	private UUID assetItemId;

	private UUID categoryId;

	@Size(max = 500, message = "Requested item detail must be 500 characters or less.")
	private String requestedItemDetail;

	@Size(max = 100, message = "Manufacturer must be 100 characters or less.")
	private String manufacturer;

	private LicenseType licenseType;

	@NotNull(message = "Quantity is required.")
	@Min(value = 1, message = "Quantity must be greater than or equal to 1.")
	private Integer quantity;

	@NotNull(message = "Expected price is required.")
	@DecimalMin(value = "0.00", message = "Expected price must be greater than or equal to 0.")
	private BigDecimal expectedPrice;

	@NotBlank(message = "Request reason is required.")
	@Size(max = 255, message = "Request reason must be 255 characters or less.")
	private String requestReason;
}
