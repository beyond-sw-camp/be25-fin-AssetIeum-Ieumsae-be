package com.ieumsae.assetieum.domain.dashboard.dto;

import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExpiringAssetDetailSearchRequest extends PaginationRequest {

	private AssetType assetType;

	private UUID departmentId;

	private String keyword;
}
