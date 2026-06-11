package com.ieumsae.assetieum.domain.intangibleasset.category.controller;

import com.ieumsae.assetieum.domain.intangibleasset.category.dto.IntangibleAssetCategoryCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.category.dto.IntangibleAssetCategoryDeleteResponse;
import com.ieumsae.assetieum.domain.intangibleasset.category.dto.IntangibleAssetCategoryResponse;
import com.ieumsae.assetieum.domain.intangibleasset.category.dto.IntangibleAssetCategoryTreeResponse;
import com.ieumsae.assetieum.domain.intangibleasset.category.service.IntangibleAssetCategoryService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/intangible-asset/categories")
public class IntangibleAssetCategoryController {
    private final IntangibleAssetCategoryService intangibleAssetCategoryService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM')")
    @PostMapping
    public ApiResponse<IntangibleAssetCategoryResponse> createCategory(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody IntangibleAssetCategoryCreateRequest request
    ){
        IntangibleAssetCategoryResponse response = intangibleAssetCategoryService.createCategory(request, member.companyId());

        return ApiResponse.created("무형자산 카테고리가 등록되었습니다.", response);
    }

    @GetMapping
    public ApiResponse<List<IntangibleAssetCategoryTreeResponse>> getIntangibleCategories(
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        List<IntangibleAssetCategoryTreeResponse> response =
                intangibleAssetCategoryService.getIntangibleCategories(member.companyId());

        return ApiResponse.ok("무형자산 카테고리 목록 조회에 성공헀습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM')")
    @DeleteMapping("/{categoryId}")
    public ApiResponse<IntangibleAssetCategoryDeleteResponse> deleteCategory(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID categoryId
    ) {
        IntangibleAssetCategoryDeleteResponse response =
                intangibleAssetCategoryService.deleteCategory(categoryId, member.companyId());

        return ApiResponse.ok("무형자산 카테고리가 삭제되었습니다.", response);
    }
}
