package com.ieumsae.assetieum.domain.ticket.rental.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RentalAssetAssignRequest {

	@NotNull
	private UUID assetId;
}
