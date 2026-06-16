package com.ieumsae.assetieum.domain.ticket.maintenance.controller;

import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceAvailableAssetResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.service.MaintenanceTicketService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
