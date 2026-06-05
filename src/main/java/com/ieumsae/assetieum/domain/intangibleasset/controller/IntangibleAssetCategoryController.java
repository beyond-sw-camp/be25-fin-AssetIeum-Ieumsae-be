package com.ieumsae.assetieum.domain.intangibleasset.controller;

import com.ieumsae.assetieum.domain.intangibleasset.dto.IntangibleAssetCategoryCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.dto.IntangibleAssetCategoryResponse;
import com.ieumsae.assetieum.domain.intangibleasset.service.IntangibleAssetCategoryService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/intangible-asset/categories")
public class IntangibleAssetCategoryController {
    private final IntangibleAssetCategoryService intangibleAssetCategoryService;

    @PostMapping
    public ApiResponse<IntangibleAssetCategoryResponse> createCategory(
            @Valid @RequestBody IntangibleAssetCategoryCreateRequest request
    ){
        IntangibleAssetCategoryResponse response = intangibleAssetCategoryService.createCategory(request);

        return ApiResponse.created("무형자산 카테고리가 등록되었습니다.", response);
    }
}
