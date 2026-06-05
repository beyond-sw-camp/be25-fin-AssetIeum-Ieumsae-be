package com.ieumsae.assetieum.domain.intangibleasset.category.controller;

import com.ieumsae.assetieum.domain.intangibleasset.category.dto.IntangibleAssetCategoryCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.category.dto.IntangibleAssetCategoryDeleteResponse;
import com.ieumsae.assetieum.domain.intangibleasset.category.dto.IntangibleAssetCategoryResponse;
import com.ieumsae.assetieum.domain.intangibleasset.category.dto.IntangibleAssetCategoryTreeResponse;
import com.ieumsae.assetieum.domain.intangibleasset.category.service.IntangibleAssetCategoryService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public ApiResponse<List<IntangibleAssetCategoryTreeResponse>> getIntangibleCategories(
            @RequestParam UUID companyId
    ) {
        List<IntangibleAssetCategoryTreeResponse> response =
                intangibleAssetCategoryService.getIntangibleCategories(companyId);

        return ApiResponse.ok("무형자산 카테고리 목록 조회에 성공헀습니다.", response);
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<IntangibleAssetCategoryDeleteResponse> deleteCategory(
        @PathVariable UUID categoryId
    ) {
        IntangibleAssetCategoryDeleteResponse response =
                intangibleAssetCategoryService.deleteCategory(categoryId);

        return ApiResponse.ok("무형자산 카테고리가 삭제되었습니다.", response);
    }
}
