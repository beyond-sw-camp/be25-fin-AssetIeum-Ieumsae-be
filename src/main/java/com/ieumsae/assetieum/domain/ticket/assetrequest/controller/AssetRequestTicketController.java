package com.ieumsae.assetieum.domain.ticket.assetrequest.controller;

import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.StandardAssetRequestCreateRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.StandardAssetRequestCreateResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.service.AssetRequestTicketService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets/asset-requests")
public class AssetRequestTicketController {

	private final AssetRequestTicketService assetRequestTicketService;

	@PostMapping("/standard")
	public ApiResponse<StandardAssetRequestCreateResponse> createStandardAssetRequest(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody StandardAssetRequestCreateRequest request
	) {
		StandardAssetRequestCreateResponse response = assetRequestTicketService.createStandardAssetRequest(
			authenticatedMember,
			request
		);
		return ApiResponse.ok("표준 자산 요청 티켓 등록에 성공했습니다.", response);
	}
}
