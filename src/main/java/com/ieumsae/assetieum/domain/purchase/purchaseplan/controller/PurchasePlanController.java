package com.ieumsae.assetieum.domain.purchase.purchaseplan.controller;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanCreateRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanDetailResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanSearchRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.service.PurchasePlanService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/purchase-plans")
public class PurchasePlanController {

    private final PurchasePlanService purchasePlanService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM')")
    @PostMapping
    public ApiResponse<PurchasePlanResponse> createPurchasePlan(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody PurchasePlanCreateRequest request
    ) {
        PurchasePlanResponse response =
                purchasePlanService.createPurchasePlan(request, member);

        return ApiResponse.ok("구매 계획이 등록되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM')")
    @DeleteMapping("/{planId}")
    public ApiResponse<PurchasePlanResponse> deletePurchasePlan(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID planId
    ) {
        PurchasePlanResponse response =
                purchasePlanService.deletePurchasePlan(planId, member.companyId());

        return ApiResponse.ok("구매 계획이 삭제되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM')")
    @GetMapping
    public ApiResponse<PaginationResponse<PurchasePlanResponse>> getPurchasePlans(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute PurchasePlanSearchRequest request
    ) {
        PaginationResponse<PurchasePlanResponse> response =
                purchasePlanService.getPurchasePlans(request, member.companyId());

        return ApiResponse.ok("구매 계획 목록 조회에 성공했습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM')")
    @GetMapping("/{planId}")
    public ApiResponse<PurchasePlanDetailResponse> getPurchasePlanDetail(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID planId
    ) {
        PurchasePlanDetailResponse response =
                purchasePlanService.getPurchasePlanDetail(planId, member.companyId());

        return ApiResponse.ok("구매 계획 상세 조회에 성공했습니다.", response);
    }

}
