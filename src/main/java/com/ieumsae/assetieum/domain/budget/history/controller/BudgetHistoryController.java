package com.ieumsae.assetieum.domain.budget.history.controller;

import com.ieumsae.assetieum.domain.budget.history.dto.BudgetHistoryResponse;
import com.ieumsae.assetieum.domain.budget.history.dto.BudgetHistorySearchRequest;
import com.ieumsae.assetieum.domain.budget.history.service.BudgetHistoryService;
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
@RequestMapping("/api/v1/budget-histories")
public class BudgetHistoryController {

    private final BudgetHistoryService budgetHistoryService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @GetMapping
    public ApiResponse<PaginationResponse<BudgetHistoryResponse>> getBudgetHistories(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute BudgetHistorySearchRequest request
    ) {
        PaginationResponse<BudgetHistoryResponse> response =
                budgetHistoryService.getBudgetHistories(request, member.companyId());

        return ApiResponse.ok("예산 이력 목록 조회에 성공했습니다.", response);
    }

}
