package com.ieumsae.assetieum.domain.ticket.common.controller;

import com.ieumsae.assetieum.domain.ticket.common.dto.PurchasePlanCandidateTicketResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.PurchasePlanCandidateTicketSearchRequest;
import com.ieumsae.assetieum.domain.ticket.common.service.PurchasePlanCandidateTicketService;
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
@RequestMapping("/api/v1/tickets/purchase-plan-candidates")
public class PurchasePlanCandidateTicketController {

    private final PurchasePlanCandidateTicketService purchasePlanCandidateTicketService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @GetMapping
    public ApiResponse<PaginationResponse<PurchasePlanCandidateTicketResponse>> getPurchasePlanCandidateTickets(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @Valid @ModelAttribute PurchasePlanCandidateTicketSearchRequest request
    ) {
        PaginationResponse<PurchasePlanCandidateTicketResponse> response =
                purchasePlanCandidateTicketService.getPurchasePlanCandidateTickets(
                        request,
                        authenticatedMember.companyId()
                );

        return ApiResponse.ok("구매 계획 후보 티켓 목록 조회에 성공했습니다.", response);
    }
}
