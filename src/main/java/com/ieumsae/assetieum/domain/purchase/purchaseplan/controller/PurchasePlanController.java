package com.ieumsae.assetieum.domain.purchase.purchaseplan.controller;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanCreateRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanDetailResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanItemCreateIntangibleAssetRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanItemCreateItemRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanItemCreateTangibleAssetRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanPurchaseResultRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanSearchRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanSearchResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanStatisticResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanUpdateStatusRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.service.PurchasePlanService;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.dto.PurchasePlanItemResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PostMapping
    public ApiResponse<PurchasePlanResponse> createPurchasePlan(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody PurchasePlanCreateRequest request
    ) {
        PurchasePlanResponse response =
                purchasePlanService.createPurchasePlan(request, member);

        return ApiResponse.ok("구매 계획이 등록되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @DeleteMapping("/{planId}")
    public ApiResponse<PurchasePlanResponse> deletePurchasePlan(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID planId
    ) {
        PurchasePlanResponse response =
                purchasePlanService.deletePurchasePlan(planId, member.companyId());

        return ApiResponse.ok("구매 계획이 삭제되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @GetMapping
    public ApiResponse<PaginationResponse<PurchasePlanSearchResponse>> getPurchasePlans(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute PurchasePlanSearchRequest request
    ) {
        PaginationResponse<PurchasePlanSearchResponse> response =
                purchasePlanService.getPurchasePlans(request, member.companyId());

        return ApiResponse.ok("구매 계획 목록 조회에 성공했습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @GetMapping("/{planId}")
    public ApiResponse<PurchasePlanDetailResponse> getPurchasePlanDetail(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID planId
    ) {
        PurchasePlanDetailResponse response =
                purchasePlanService.getPurchasePlanDetail(planId, member.companyId());

        return ApiResponse.ok("구매 계획 상세 조회에 성공했습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @GetMapping("/statistics")
    public ApiResponse<PurchasePlanStatisticResponse> getPurchasePlanStatistics(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        PurchasePlanStatisticResponse response =
                purchasePlanService.getPurchasePlanStatistics(member.companyId());

        return ApiResponse.ok("구매 계획 통계 조회에 성공했습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PatchMapping("/{planId}/status")
    public ApiResponse<PurchasePlanResponse> updatePurchasePlanStatus(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID planId,
            @Valid @RequestBody PurchasePlanUpdateStatusRequest request
    ) {
        PurchasePlanResponse response =
                purchasePlanService.updatePurchasePlanStatus(planId, request, member.companyId());

        return ApiResponse.ok("구매 계획의 상태가 변경되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PatchMapping("/{planId}/purchase-result")
    public ApiResponse<PurchasePlanDetailResponse> updatePurchasePlanPurchaseResult(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID planId,
            @Valid @RequestBody PurchasePlanPurchaseResultRequest request
    ) {
        PurchasePlanDetailResponse response =
                purchasePlanService.updatePurchasePlanPurchaseResult(planId, request, member.companyId());

        return ApiResponse.ok("구매 계획 실제 결제 금액이 등록되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PatchMapping("/{planId}/items/{itemId}/confirm")
    public ApiResponse<PurchasePlanItemResponse> updatePurchasePlanItemStatus(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID planId,
            @PathVariable Long itemId
    ) {
        PurchasePlanItemResponse response =
                purchasePlanService.updatePurchasePlanItemStatus(planId, itemId, member.companyId());

        return ApiResponse.ok("구매 계획 품목이 납품 확인이 되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PostMapping("/{planId}/items/{itemId}")
    public ApiResponse<Void> createItemFromPurchasePlan(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID planId,
            @PathVariable Long itemId,
            @Valid @RequestBody PurchasePlanItemCreateItemRequest request
    ) {
        purchasePlanService.createItemFromPurchasePlan(planId, itemId, member.companyId(), request);

        return ApiResponse.ok("구매 계획의 품목이 등록되었습니다.", null);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PostMapping("/{planId}/items/{itemId}/tangible-assets")
    public ApiResponse<Void> createTangibleAssetFromPurchasePlan(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID planId,
            @PathVariable Long itemId,
            @Valid @RequestBody PurchasePlanItemCreateTangibleAssetRequest request
    ) {
        purchasePlanService.createTangibleAssetFromPurchasePlan(planId, itemId, member.companyId(), request);

        return ApiResponse.ok("구매 계획의 유형자산이 등록되었습니다.", null);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PostMapping("/{planId}/items/{itemId}/intangible-assets")
    public ApiResponse<Void> createIntangibleAssetFromPurchasePlan(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID planId,
            @PathVariable Long itemId,
            @Valid @RequestBody PurchasePlanItemCreateIntangibleAssetRequest request
    ) {
        purchasePlanService.createIntangibleAssetFromPurchasePlan(planId, itemId, member.companyId(), request);

        return ApiResponse.ok("구매 계획의 무형자산이 등록되었습니다.", null);
    }

}
