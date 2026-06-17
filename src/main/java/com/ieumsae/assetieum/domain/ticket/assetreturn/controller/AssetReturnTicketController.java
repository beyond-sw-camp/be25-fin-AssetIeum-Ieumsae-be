package com.ieumsae.assetieum.domain.ticket.assetreturn.controller;

import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnAvailableAssetResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnAvailableAssetSearchRequest;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.service.AssetReturnTicketService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets/asset-returns")
public class AssetReturnTicketController {

	private final AssetReturnTicketService assetReturnTicketService;

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/available-assets")
	public ApiResponse<List<AssetReturnAvailableAssetResponse>> getAvailableAssets(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute AssetReturnAvailableAssetSearchRequest request
	) {
		List<AssetReturnAvailableAssetResponse> response = assetReturnTicketService.getAvailableAssets(
			authenticatedMember,
			request
		);

		return ApiResponse.ok("반납/해지 요청 가능 자산 목록 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PostMapping
	public ApiResponse<AssetReturnTicketCreateResponse> createAssetReturnTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody AssetReturnTicketCreateRequest request
	) {
		AssetReturnTicketCreateResponse response = assetReturnTicketService.createAssetReturnTicket(
			authenticatedMember,
			request
		);

		return ApiResponse.created("자산 반납/해지 요청 티켓 등록에 성공했습니다.", response);
	}
}
