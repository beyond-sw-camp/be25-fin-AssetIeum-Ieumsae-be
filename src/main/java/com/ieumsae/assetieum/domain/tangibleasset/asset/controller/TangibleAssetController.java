package com.ieumsae.assetieum.domain.tangibleasset.asset.controller;

import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.service.TangibleAssetService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
