package com.ieumsae.assetieum.domain.ticket.assetrequest.controller;

import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.service.AssetRequestTicketService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets/asset-requests")
public class AssetRequestTicketController {

	private final AssetRequestTicketService assetRequestTicketService;

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PostMapping
	public ApiResponse<AssetRequestTicketCreateResponse> createAssetRequestTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody AssetRequestTicketCreateRequest request
	) {
		AssetRequestTicketCreateResponse response = assetRequestTicketService.createAssetRequestTicket(
			authenticatedMember,
			request
		);
		return ApiResponse.ok("자산 요청 티켓 등록에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/{ticketId}")
	public ApiResponse<AssetRequestTicketDetailResponse> getAssetRequestTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		AssetRequestTicketDetailResponse response = assetRequestTicketService.getAssetRequestTicket(
			authenticatedMember,
			ticketId
		);
		return ApiResponse.ok("자산 요청 티켓 상세 조회에 성공했습니다.", response);
	}
}
