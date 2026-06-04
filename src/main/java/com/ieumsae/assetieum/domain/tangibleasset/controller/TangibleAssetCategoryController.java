package com.ieumsae.assetieum.domain.tangibleasset.controller;

import com.ieumsae.assetieum.domain.tangibleasset.dto.TangibleAssetCategoryCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.dto.TangibleAssetCategoryResponse;
import com.ieumsae.assetieum.domain.tangibleasset.service.TangibleAssetCategoryService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tangible-asset/categories")
public class TangibleAssetCategoryController {
    private final TangibleAssetCategoryService tangibleAssetCategoryService;

    @PostMapping
    public ApiResponse<TangibleAssetCategoryResponse> createCategory(
            @Valid @RequestBody TangibleAssetCategoryCreateRequest request
    ) {
        TangibleAssetCategoryResponse response = tangibleAssetCategoryService.createCategory(request);

        return ApiResponse.created("유형자산 카테고리가 등록되었습니다.", response);
    }
}
