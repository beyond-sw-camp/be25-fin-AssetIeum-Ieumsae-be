package com.ieumsae.assetieum.domain.tangibleasset.asset.controller;

import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetDetailResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetUpdateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.service.TangibleAssetService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/tangible-asset/assets")
public class TangibleAssetController {

    private final TangibleAssetService tangibleAssetService;

    @PostMapping
    public ApiResponse<TangibleAssetResponse> createAsset(
            @Valid @RequestBody TangibleAssetCreateRequest request
    ) {
        TangibleAssetResponse response = tangibleAssetService.createAsset(request);

        return ApiResponse.ok("유형자산이 등록되었습니다.", response);
    }

    @GetMapping
    public ApiResponse<PaginationResponse<TangibleAssetSearchResponse>> getAssets(
            @Valid @ModelAttribute TangibleAssetSearchRequest request
    ) {
        PaginationResponse<TangibleAssetSearchResponse> response = tangibleAssetService.getAssets(request);

        return ApiResponse.ok("유형자산 목록 조회에 성공했습니다.", response);
    }

    @GetMapping({"/{assetId}"})
    public ApiResponse<TangibleAssetDetailResponse> getAssetDetail(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId
    ) {
        TangibleAssetDetailResponse response = tangibleAssetService.getAssetDetail(assetId, member.companyId());
        return ApiResponse.ok("유형자산 상세 조회에 성공했습니다.", response);
    }

    @PatchMapping("/{assetId}")
    public ApiResponse<TangibleAssetResponse> updateAsset(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId,
            @Valid @RequestBody TangibleAssetUpdateRequest request
    ){
        TangibleAssetResponse response =
                tangibleAssetService.updateAsset(assetId, request, member.companyId());

        return ApiResponse.ok("유형자산이 수정되었습니다.", response);
    }

}
