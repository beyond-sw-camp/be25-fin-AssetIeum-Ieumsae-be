package com.ieumsae.assetieum.domain.ticket.assetreturn.controller;

import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnAvailableAssetResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnAvailableAssetSearchRequest;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnCollectResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnCompleteResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.service.AssetReturnTicketService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	@GetMapping("/{ticketId}")
	public ApiResponse<AssetReturnTicketDetailResponse> getAssetReturnTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		AssetReturnTicketDetailResponse response = assetReturnTicketService.getAssetReturnTicket(
			authenticatedMember,
			ticketId
		);

		return ApiResponse.ok("자산 반납/해지 티켓 상세 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PatchMapping("/{ticketId}/collect")
	public ApiResponse<AssetReturnCollectResponse> collectAssetReturn(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		AssetReturnCollectResponse response = assetReturnTicketService.collectAssetReturn(
			authenticatedMember,
			ticketId
		);

		return ApiResponse.ok("반납 자산 회수 처리에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PatchMapping("/{ticketId}/complete")
	public ApiResponse<AssetReturnCompleteResponse> completeAssetReturn(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		AssetReturnCompleteResponse response = assetReturnTicketService.completeAssetReturn(
			authenticatedMember,
			ticketId
		);

		return ApiResponse.ok("자산 반납/해지 완료 처리에 성공했습니다.", response);
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
