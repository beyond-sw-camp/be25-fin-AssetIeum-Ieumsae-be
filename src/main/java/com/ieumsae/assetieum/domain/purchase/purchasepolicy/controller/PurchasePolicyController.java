package com.ieumsae.assetieum.domain.purchase.purchasepolicy.controller;

import com.ieumsae.assetieum.domain.purchase.purchasepolicy.dto.PurchasePolicyRequest;
import com.ieumsae.assetieum.domain.purchase.purchasepolicy.dto.PurchasePolicyResponse;
import com.ieumsae.assetieum.domain.purchase.purchasepolicy.service.PurchasePolicyService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/purchase-policies")
public class PurchasePolicyController {

    private final PurchasePolicyService purchasePolicyService;

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ApiResponse<PurchasePolicyResponse> updatePurchasePolicy(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody PurchasePolicyRequest request
    ) {
        PurchasePolicyResponse response = purchasePolicyService.updatePurchasePolicy(
            request, member.companyId()
        );

        return ApiResponse.ok("구매 정책이 설정되었습니다.", response);
    }
}
