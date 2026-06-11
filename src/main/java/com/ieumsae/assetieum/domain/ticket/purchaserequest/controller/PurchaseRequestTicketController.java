package com.ieumsae.assetieum.domain.ticket.purchaserequest.controller;

import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.service.PurchaseRequestTicketService;
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
@RequestMapping("/api/v1/tickets/purchase-requests")
public class PurchaseRequestTicketController {

	private final PurchaseRequestTicketService purchaseRequestTicketService;

	@PostMapping("/non-standard")
	public ApiResponse<PurchaseRequestTicketCreateResponse> createNonStandardPurchaseRequestTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody PurchaseRequestTicketCreateRequest request
	) {
		PurchaseRequestTicketCreateResponse response = purchaseRequestTicketService.createTeamPurchaseRequestTicket(
			authenticatedMember,
			request
		);
		return ApiResponse.ok("구매 요청 티켓 등록에 성공했습니다.", response);
	}

	@PostMapping("/direct-purchase")
	public ApiResponse<PurchaseRequestTicketCreateResponse> createDirectPurchaseRequestTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody DirectPurchaseRequestTicketCreateRequest request
	) {
		PurchaseRequestTicketCreateResponse response = purchaseRequestTicketService.createDirectPurchaseRequestTicket(
			authenticatedMember,
			request
		);
		return ApiResponse.ok("직접 구매 요청 티켓 등록에 성공했습니다.", response);
	}
}
