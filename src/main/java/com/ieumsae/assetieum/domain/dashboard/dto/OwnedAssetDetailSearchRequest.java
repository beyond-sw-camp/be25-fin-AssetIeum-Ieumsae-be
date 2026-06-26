package com.ieumsae.assetieum.domain.dashboard.dto;

import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OwnedAssetDetailSearchRequest extends PaginationRequest {

	@NotNull
	private OwnedAssetDetailStatus status;

	private UUID departmentId;

	private String keyword;
}
