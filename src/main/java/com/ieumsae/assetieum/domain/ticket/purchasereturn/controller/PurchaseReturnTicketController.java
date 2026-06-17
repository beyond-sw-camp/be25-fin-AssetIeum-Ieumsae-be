package com.ieumsae.assetieum.domain.ticket.purchasereturn.controller;

import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnAvailableAssetResponse;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnAvailableAssetSearchRequest;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.service.PurchaseReturnTicketService;
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
@RequestMapping("/api/v1/tickets/purchase-returns")
public class PurchaseReturnTicketController {

	private final PurchaseReturnTicketService purchaseReturnTicketService;

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/available-assets")
	public ApiResponse<List<PurchaseReturnAvailableAssetResponse>> getAvailableAssets(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute PurchaseReturnAvailableAssetSearchRequest request
	) {
		List<PurchaseReturnAvailableAssetResponse> response = purchaseReturnTicketService.getAvailableAssets(
			authenticatedMember,
			request
		);

		return ApiResponse.ok("반품 요청 가능 자산 목록 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PostMapping
	public ApiResponse<PurchaseReturnTicketCreateResponse> createPurchaseReturnTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody PurchaseReturnTicketCreateRequest request
	) {
		PurchaseReturnTicketCreateResponse response = purchaseReturnTicketService.createPurchaseReturnTicket(
			authenticatedMember,
			request
		);

		return ApiResponse.created("반품 요청 티켓 등록에 성공했습니다.", response);
	}
}
