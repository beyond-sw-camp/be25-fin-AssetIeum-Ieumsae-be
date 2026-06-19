package com.ieumsae.assetieum.domain.ticket.common.controller;

import com.ieumsae.assetieum.domain.ticket.common.dto.AssetApprovalResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.DepartmentApprovalResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketAssigneeResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketCancelResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketListItemResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketProcessingStatusUpdateRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketProcessingStatusUpdateResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketRejectionRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketSearchRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketStatisticsResponse;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets")
public class TicketController {

	private final TicketService ticketService;

	@PreAuthorize("isAuthenticated()")
	@GetMapping
	public ApiResponse<PaginationResponse<TicketListItemResponse>> getTickets(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute TicketSearchRequest request
	) {
		PaginationResponse<TicketListItemResponse> response = ticketService.getTickets(
			authenticatedMember,
			request
		);

		return ApiResponse.ok("티켓 목록 조회에 성공했습니다.", response);
	}

	@PreAuthorize("isAuthenticated()")
	@PatchMapping("/{ticketId}/assign-me")
	public ApiResponse<TicketAssigneeResponse> assignMe(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		TicketAssigneeResponse response = ticketService.assignMe(authenticatedMember, ticketId);

		return ApiResponse.ok("티켓 담당자 지정에 성공했습니다.", response);
	}

	@PreAuthorize("isAuthenticated()")
	@PatchMapping("/{ticketId}/cancel")
	public ApiResponse<TicketCancelResponse> cancelTicket(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		TicketCancelResponse response = ticketService.cancelTicket(authenticatedMember, ticketId);

		return ApiResponse.ok("티켓 취소에 성공했습니다.", response);
	}

	@PreAuthorize("isAuthenticated()")
	@PatchMapping("/{ticketId}/department-approval/approve")
	public ApiResponse<DepartmentApprovalResponse> approveDepartment(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		DepartmentApprovalResponse response = ticketService.approveDepartment(
			authenticatedMember,
			ticketId
		);

		return ApiResponse.ok("부서장 승인 처리에 성공했습니다.", response);
	}

	@PreAuthorize("isAuthenticated()")
	@PatchMapping("/{ticketId}/department-approval/reject")
	public ApiResponse<DepartmentApprovalResponse> rejectDepartment(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @RequestBody TicketRejectionRequest request
	) {
		DepartmentApprovalResponse response = ticketService.rejectDepartment(
			authenticatedMember,
			ticketId,
			request
		);

		return ApiResponse.ok("부서장 반려 처리에 성공했습니다.", response);
	}

	@PreAuthorize("isAuthenticated()")
	@PatchMapping("/{ticketId}/asset-approval/approve")
	public ApiResponse<AssetApprovalResponse> approveAsset(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId
	) {
		AssetApprovalResponse response = ticketService.approveAsset(
			authenticatedMember,
			ticketId
		);

		return ApiResponse.ok("구매자산팀 승인 처리에 성공했습니다.", response);
	}

	@PreAuthorize("isAuthenticated()")
	@PatchMapping("/{ticketId}/asset-approval/reject")
	public ApiResponse<AssetApprovalResponse> rejectAsset(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @RequestBody TicketRejectionRequest request
	) {
		AssetApprovalResponse response = ticketService.rejectAsset(
			authenticatedMember,
			ticketId,
			request
		);

		return ApiResponse.ok("구매자산팀 반려 처리에 성공했습니다.", response);
	}

	@PreAuthorize("isAuthenticated()")
	@PatchMapping("/{ticketId}/processing-status")
	public ApiResponse<TicketProcessingStatusUpdateResponse> changeProcessingStatus(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @RequestBody TicketProcessingStatusUpdateRequest request
	) {
		TicketProcessingStatusUpdateResponse response = ticketService.changeProcessingStatus(
			authenticatedMember,
			ticketId,
			request
		);

		return ApiResponse.ok("티켓 처리상태 변경에 성공했습니다.", response);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/statistics")
	public ApiResponse<TicketStatisticsResponse> getTicketStatistics(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		TicketStatisticsResponse response = ticketService.getTicketStatistics(authenticatedMember);

		return ApiResponse.ok("티켓 통계 조회에 성공했습니다.", response);
	}
}
