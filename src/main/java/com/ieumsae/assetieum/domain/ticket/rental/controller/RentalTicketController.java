package com.ieumsae.assetieum.domain.ticket.rental.controller;

import com.ieumsae.assetieum.domain.tangibleasset.item.dto.AvailableRentalItemResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.ActiveRentalAssetResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.AvailableRentalItemSearchRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalAssetAssignRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalAssetAssignResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalAssignableAssetSearchRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalAssignableAssetsResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalExtensionTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalExtensionTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalExtensionDueDateUpdateRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalExtensionDueDateUpdateResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.rental.service.RentalTicketService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets/rentals")
public class RentalTicketController {

	private final RentalTicketService rentalTicketService;

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/available-items")
	public ApiResponse<PaginationResponse<AvailableRentalItemResponse>> getAvailableRentalItems(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute AvailableRentalItemSearchRequest request
	) {
		PaginationResponse<AvailableRentalItemResponse> response = rentalTicketService.getAvailableRentalItems(
			authenticatedMember,
			request
		);

		return ApiResponse.ok("대여 가능 품목 목록 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/active-assets")
	public ApiResponse<List<ActiveRentalAssetResponse>> getActiveRentalAssets(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		List<ActiveRentalAssetResponse> response = rentalTicketService.getActiveRentalAssets(authenticatedMember);

		return ApiResponse.ok("대여중인 자산 목록 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/{ticketId}")
	public ApiResponse<RentalTicketDetailResponse> getRentalTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		RentalTicketDetailResponse response = rentalTicketService.getRentalTicket(
			authenticatedMember,
			ticketId
		);

		return ApiResponse.ok("대여 티켓 상세 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/{ticketId}/assignable-assets")
	public ApiResponse<RentalAssignableAssetsResponse> getAssignableAssets(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @ModelAttribute RentalAssignableAssetSearchRequest request
	) {
		RentalAssignableAssetsResponse response = rentalTicketService.getAssignableAssets(
			authenticatedMember,
			ticketId,
			request
		);

		return ApiResponse.ok("대여자산 할당 후보 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PostMapping("/{ticketId}/assign")
	public ApiResponse<RentalAssetAssignResponse> assignRentalAsset(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @RequestBody RentalAssetAssignRequest request
	) {
		RentalAssetAssignResponse response = rentalTicketService.assignRentalAsset(
			authenticatedMember,
			ticketId,
			request
		);

		return ApiResponse.ok("대여 자산 할당에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PostMapping
	public ApiResponse<RentalTicketCreateResponse> createRentalTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody RentalTicketCreateRequest request
	) {
		RentalTicketCreateResponse response = rentalTicketService.createRentalTicket(
			authenticatedMember,
			request
		);

		return ApiResponse.ok("대여 자산 요청 티켓 등록에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/extensions/{ticketId}")
	public ApiResponse<RentalTicketDetailResponse> getRentalExtensionTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		RentalTicketDetailResponse response = rentalTicketService.getRentalExtensionTicket(
			authenticatedMember,
			ticketId
		);

		return ApiResponse.ok("대여연장 티켓 상세 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PatchMapping("/extensions/{ticketId}/return-due-date")
	public ApiResponse<RentalExtensionDueDateUpdateResponse> updateRentalExtensionDueDate(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @RequestBody RentalExtensionDueDateUpdateRequest request
	) {
		RentalExtensionDueDateUpdateResponse response = rentalTicketService.updateRentalExtensionDueDate(
			authenticatedMember,
			ticketId,
			request
		);

		return ApiResponse.ok("대여연장 반납 예정일 변경에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PostMapping("/extensions")
	public ApiResponse<RentalExtensionTicketCreateResponse> createRentalExtensionTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody RentalExtensionTicketCreateRequest request
	) {
		RentalExtensionTicketCreateResponse response = rentalTicketService.createRentalExtensionTicket(
			authenticatedMember,
			request
		);

		return ApiResponse.created("대여 연장 요청 티켓 등록에 성공했습니다.", response);
	}
}
