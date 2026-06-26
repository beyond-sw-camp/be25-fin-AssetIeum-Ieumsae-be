package com.ieumsae.assetieum.domain.ticket.purchaserequest.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DirectPurchaseAssetAssignRequest {

	private UUID itemId;

	@Size(max = 255)
	private String productName;

	@Size(max = 100)
	private String manufacturer;

	@Size(max = 100)
	private String modelName;

	@Size(max = 100)
	private String provider;

}
