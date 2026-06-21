package com.ieumsae.assetieum.domain.ticket.maintenance.controller;

import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceAvailableAssetResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceAssetCollectResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCompleteRequest;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCompleteResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.service.MaintenanceTicketService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets/maintenance")
public class MaintenanceTicketController {

	private final MaintenanceTicketService maintenanceTicketService;

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/available-assets")
	public ApiResponse<List<MaintenanceAvailableAssetResponse>> getAvailableAssets(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		List<MaintenanceAvailableAssetResponse> response = maintenanceTicketService.getAvailableAssets(
			authenticatedMember
		);

		return ApiResponse.ok("유지보수 요청 가능 자산 목록 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/{ticketId}")
	public ApiResponse<MaintenanceTicketDetailResponse> getMaintenanceTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		MaintenanceTicketDetailResponse response = maintenanceTicketService.getMaintenanceTicket(
			authenticatedMember,
			ticketId
		);

		return ApiResponse.ok("유지보수 티켓 상세 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PatchMapping("/{ticketId}/collect")
	public ApiResponse<MaintenanceAssetCollectResponse> collectMaintenanceAsset(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		MaintenanceAssetCollectResponse response = maintenanceTicketService.collectMaintenanceAsset(
			authenticatedMember,
			ticketId
		);

		return ApiResponse.ok("유지보수 자산 회수 처리에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PatchMapping("/{ticketId}/complete")
	public ApiResponse<MaintenanceTicketCompleteResponse> completeMaintenanceTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @RequestBody MaintenanceTicketCompleteRequest request
	) {
		MaintenanceTicketCompleteResponse response = maintenanceTicketService.completeMaintenanceTicket(
			authenticatedMember,
			ticketId,
			request
		);

		return ApiResponse.ok("유지보수 완료 처리에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPARTMENT_MANAGER', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@PostMapping
	public ApiResponse<MaintenanceTicketCreateResponse> createMaintenanceTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody MaintenanceTicketCreateRequest request
	) {
		MaintenanceTicketCreateResponse response = maintenanceTicketService.createMaintenanceTicket(
			authenticatedMember,
			request
		);

		return ApiResponse.created("유지보수 요청 티켓 등록에 성공했습니다.", response);
	}
}
