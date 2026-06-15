package com.ieumsae.assetieum.domain.budget.budget.controller;

import com.ieumsae.assetieum.domain.budget.budget.dto.BudgetResponse;
import com.ieumsae.assetieum.domain.budget.budget.dto.BudgetSearchRequest;
import com.ieumsae.assetieum.domain.budget.budget.service.BudgetService;
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
@RequestMapping("/api/v1/budgets")
public class budgetController {

    private final BudgetService budgetService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM')")
    @GetMapping
    public ApiResponse<PaginationResponse<BudgetResponse>> getBudgets(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute BudgetSearchRequest request
    ) {
        PaginationResponse<BudgetResponse> response =
                budgetService.getBudgets(request, member.companyId());

        return ApiResponse.ok("예산 목록 조회에 성공했습니다.", response);
    }
}
