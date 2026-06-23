package com.ieumsae.assetieum.domain.budget.budget.controller;

import com.ieumsae.assetieum.domain.budget.budget.dto.BudgetResponse;
import com.ieumsae.assetieum.domain.budget.budget.dto.BudgetSearchRequest;
import com.ieumsae.assetieum.domain.budget.budget.service.BudgetService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping()
public class budgetController {

    private final BudgetService budgetService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @GetMapping("/api/v1/budgets")
    public ApiResponse<PaginationResponse<BudgetResponse>> getBudgets(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute BudgetSearchRequest request
    ) {
        PaginationResponse<BudgetResponse> response =
                budgetService.getBudgets(request, member.companyId());

        return ApiResponse.ok("예산 목록 조회에 성공했습니다.", response);
    }

    @GetMapping("/api/v1/departments/{departmentId}/budgets")
    public ApiResponse<BudgetResponse> getDepartmentBudget(
            @AuthenticationPrincipal AuthenticatedMember member,
            @NotNull @RequestParam Integer budgetYear,
            @PathVariable UUID departmentId
    ) {
        BudgetResponse response =
                budgetService.getDepartmentBudget(budgetYear, departmentId, member);

        return ApiResponse.ok("부서별 예산 조회에 성공했습니다.", response);
    }
}
