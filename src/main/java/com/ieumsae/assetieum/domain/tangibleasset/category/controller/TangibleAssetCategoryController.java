package com.ieumsae.assetieum.domain.tangibleasset.category.controller;

import com.ieumsae.assetieum.domain.tangibleasset.category.dto.TangibleAssetCategoryCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.category.dto.TangibleAssetCategoryDeleteResponse;
import com.ieumsae.assetieum.domain.tangibleasset.category.dto.TangibleAssetCategoryResponse;
import com.ieumsae.assetieum.domain.tangibleasset.category.dto.TangibleAssetCategoryTreeResponse;
import com.ieumsae.assetieum.domain.tangibleasset.category.service.TangibleAssetCategoryService;
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
@RequestMapping("/api/v1/tangible-asset/categories")
public class TangibleAssetCategoryController {
    private final TangibleAssetCategoryService tangibleAssetCategoryService;

    @PostMapping
    public ApiResponse<TangibleAssetCategoryResponse> createCategory(
            @Valid @RequestBody TangibleAssetCategoryCreateRequest request
    ) {
        TangibleAssetCategoryResponse response = tangibleAssetCategoryService.createCategory(request);

        return ApiResponse.ok("유형자산 카테고리가 등록되었습니다.", response);
    }

    @GetMapping
    public ApiResponse<List<TangibleAssetCategoryTreeResponse>> getTangibleCategories(
            @RequestParam UUID companyId
    ) {
        List<TangibleAssetCategoryTreeResponse> response =
                tangibleAssetCategoryService.getTangibleCategories(companyId);

        return ApiResponse.ok("유형자산 카테고리 목록 조회에 성공했습니다.", response);
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<TangibleAssetCategoryDeleteResponse> deleteCategory(
            @PathVariable UUID categoryId
    ) {
        TangibleAssetCategoryDeleteResponse response =
                tangibleAssetCategoryService.deleteCategory(categoryId);

        return ApiResponse.ok("유형자산 카테고리가 삭제되었습니다.", response);
    }
}
