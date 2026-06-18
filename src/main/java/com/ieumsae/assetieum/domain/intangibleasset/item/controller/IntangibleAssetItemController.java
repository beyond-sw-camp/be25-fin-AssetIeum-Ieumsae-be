package com.ieumsae.assetieum.domain.intangibleasset.item.controller;

import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemDeleteResponse;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemSearchRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemUpdateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.service.IntangibleAssetItemService;
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
@RequestMapping("/api/v1/intangible-asset/items")
public class IntangibleAssetItemController {
    private final IntangibleAssetItemService intangibleAssetItemService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PostMapping
    public ApiResponse<IntangibleAssetItemResponse> createItem(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody IntangibleAssetItemCreateRequest request
    ) {
        IntangibleAssetItemResponse response =
                intangibleAssetItemService.createItem(request, member.companyId());

        return ApiResponse.ok("무형자산 품목이 등록되었습니다.", response);
    }

    @GetMapping
    public ApiResponse<PaginationResponse<IntangibleAssetItemResponse>> getItems(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute IntangibleAssetItemSearchRequest request
    ) {
        PaginationResponse<IntangibleAssetItemResponse> response =
                intangibleAssetItemService.getItems(request, member.companyId());

        return ApiResponse.ok("무형자산 품목 목록 조회에 성공했습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PatchMapping("/{itemId}")
    public ApiResponse<IntangibleAssetItemResponse> updateItem(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID itemId,
            @Valid @RequestBody IntangibleAssetItemUpdateRequest request
    ) {
        IntangibleAssetItemResponse response =
                intangibleAssetItemService.updateItem(itemId, request, member.companyId());

        return ApiResponse.ok("무형자산 품목이 수정되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @DeleteMapping("/{itemId}")
    public ApiResponse<IntangibleAssetItemDeleteResponse> deleteItem(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID itemId
    ) {
        IntangibleAssetItemDeleteResponse response = intangibleAssetItemService.deleteItem(itemId, member.companyId());

        return ApiResponse.ok("무형자산 품목이 삭제되었습니다.", response);
    }
}
