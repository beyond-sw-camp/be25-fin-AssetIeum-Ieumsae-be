package com.ieumsae.assetieum.domain.purchase.purchaseplan.controller;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanCreateRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.service.PurchasePlanService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
