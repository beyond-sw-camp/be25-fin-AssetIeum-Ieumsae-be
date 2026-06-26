package com.ieumsae.assetieum.domain.ticket.assetrequest.dto;

import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AssetRequestAssignRequest {

	@NotNull
	private AssetType assetType;

	@NotNull
	private UUID itemId;

	private List<UUID> assigneeIds;
}
