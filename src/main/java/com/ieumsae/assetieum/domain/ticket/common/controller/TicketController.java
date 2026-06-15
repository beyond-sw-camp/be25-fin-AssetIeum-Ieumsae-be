package com.ieumsae.assetieum.domain.ticket.common.controller;

import com.ieumsae.assetieum.domain.ticket.common.dto.TicketListItemResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketSearchRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketStatisticsResponse;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	@GetMapping("/statistics")
	public ApiResponse<TicketStatisticsResponse> getTicketStatistics(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		TicketStatisticsResponse response = ticketService.getTicketStatistics(authenticatedMember);

		return ApiResponse.ok("티켓 통계 조회에 성공했습니다.", response);
	}
}
