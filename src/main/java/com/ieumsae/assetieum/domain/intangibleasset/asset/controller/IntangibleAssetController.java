package com.ieumsae.assetieum.domain.intangibleasset.asset.controller;

import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetDetailResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetSearchRequest;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetUpdateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.asset.service.IntangibleAssetService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/v1/intangible-asset/assets")
public class IntangibleAssetController {

    private final IntangibleAssetService intangibleAssetService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PostMapping
    public ApiResponse<IntangibleAssetResponse> createItem(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody IntangibleAssetCreateRequest request
    ) {
        IntangibleAssetResponse response =
                intangibleAssetService.createAsset(request, member.companyId());

        return ApiResponse.ok("무형자산이 등록되었습니다.", response);
    }

    @GetMapping
    public ApiResponse<PaginationResponse<IntangibleAssetSearchResponse>> getAssets(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute IntangibleAssetSearchRequest request
    ) {
        PaginationResponse<IntangibleAssetSearchResponse> response =
                intangibleAssetService.getAssets(request, member.companyId());

        return ApiResponse.ok("무형자산 목록 조회에 성공했습니다.", response);
    }

    @GetMapping("/{assetId}")
    public ApiResponse<IntangibleAssetDetailResponse> getAssetDetail(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId
    ) {
        IntangibleAssetDetailResponse response = intangibleAssetService.getAssetDetail(assetId, member.companyId());

        return ApiResponse.ok("무형자산 상세 조회에 성공했습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PatchMapping("/{assetId}")
    public ApiResponse<IntangibleAssetResponse> updateAsset(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId,
            @Valid @RequestBody IntangibleAssetUpdateRequest request
    ) {
        IntangibleAssetResponse response =
                intangibleAssetService.updateAsset(assetId, request, member.companyId());

        return ApiResponse.ok("무형자산이 수정되었습니다.", response);
    }

}
