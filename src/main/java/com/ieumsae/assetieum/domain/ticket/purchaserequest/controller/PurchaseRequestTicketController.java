package com.ieumsae.assetieum.domain.ticket.purchaserequest.controller;

import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseAssetAssignRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseAssetAssignResponse;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseResultCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseResultCreateResponse;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.service.PurchaseRequestTicketService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets/purchase-requests")
public class PurchaseRequestTicketController {

	private final PurchaseRequestTicketService purchaseRequestTicketService;

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
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

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
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
	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PostMapping("/{ticketId}/direct-purchase-result")
	public ApiResponse<DirectPurchaseResultCreateResponse> createDirectPurchaseResult(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @RequestBody DirectPurchaseResultCreateRequest request
	) {
		DirectPurchaseResultCreateResponse response = purchaseRequestTicketService.createDirectPurchaseResult(
			authenticatedMember,
			ticketId,
			request
		);
		return ApiResponse.ok("직접구매 완료 정보 등록에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PutMapping("/{ticketId}/direct-purchase-result")
	public ApiResponse<DirectPurchaseResultCreateResponse> updateDirectPurchaseResult(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @RequestBody DirectPurchaseResultCreateRequest request
	) {
		DirectPurchaseResultCreateResponse response = purchaseRequestTicketService.updateDirectPurchaseResult(
			authenticatedMember,
			ticketId,
			request
		);
		return ApiResponse.ok("직접구매 완료 정보 수정에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/{ticketId}")
	public ApiResponse<PurchaseRequestTicketDetailResponse> getPurchaseRequestTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		PurchaseRequestTicketDetailResponse response = purchaseRequestTicketService.getPurchaseRequestTicket(
			authenticatedMember,
			ticketId
		);
		return ApiResponse.ok("구매 요청 티켓 상세 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/{ticketId}/direct-purchase-result")
	public ApiResponse<DirectPurchaseResultCreateResponse> getDirectPurchaseResult(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		DirectPurchaseResultCreateResponse response = purchaseRequestTicketService.getDirectPurchaseResult(
			authenticatedMember,
			ticketId
		);
		return ApiResponse.ok("직접구매 결제정보 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PatchMapping("/{ticketId}/direct-purchase-result/confirm")
	public ApiResponse<DirectPurchaseResultCreateResponse> confirmDirectPurchaseResult(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		DirectPurchaseResultCreateResponse response = purchaseRequestTicketService.confirmDirectPurchaseResult(
			authenticatedMember,
			ticketId
		);
		return ApiResponse.ok("직접구매 증빙 확인완료 처리에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PostMapping("/{ticketId}/direct-purchase-assets/assign")
	public ApiResponse<DirectPurchaseAssetAssignResponse> assignDirectPurchaseAsset(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @RequestBody DirectPurchaseAssetAssignRequest request
	) {
		DirectPurchaseAssetAssignResponse response = purchaseRequestTicketService.assignDirectPurchaseAsset(
			authenticatedMember,
			ticketId,
			request
		);
		return ApiResponse.ok("직접구매 자산 등록 및 할당에 성공했습니다.", response);
	}
}
