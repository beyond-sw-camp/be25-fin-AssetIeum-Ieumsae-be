package com.ieumsae.assetieum.domain.tangibleasset.item.controller;

import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemSearchRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemUpdateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.service.TangibleAssetItemService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tangible-asset/items")
public class TangibleAssetItemController {

    private final TangibleAssetItemService tangibleAssetItemService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PostMapping
    public ApiResponse<TangibleAssetItemResponse> createItem(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody TangibleAssetItemCreateRequest request
    ) {
        TangibleAssetItemResponse response =
                tangibleAssetItemService.createItem(request, member.companyId());

        return ApiResponse.ok("유형자산 품목이 등록되었습니다.", response);
    }

    @GetMapping
    public ApiResponse<PaginationResponse<TangibleAssetItemResponse>> getItems(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute TangibleAssetItemSearchRequest request
    ) {
        PaginationResponse<TangibleAssetItemResponse> response =
                tangibleAssetItemService.getItems(request, member.companyId());

        return ApiResponse.ok("유형자산 품목 목록 조회에 성공했습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PatchMapping("/{itemId}")
    public ApiResponse<TangibleAssetItemResponse> updateItem(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID itemId,
            @Valid @RequestBody TangibleAssetItemUpdateRequest request
    ) {
        TangibleAssetItemResponse response =
                tangibleAssetItemService.updateItem(itemId, request, member.companyId());

        return ApiResponse.ok("유형자산 품목이 수정되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @DeleteMapping("/{itemId}")
    public ApiResponse<Void> deleteItem(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID itemId
    ) {
        tangibleAssetItemService.deleteItem(itemId, member.companyId());

        return ApiResponse.ok("유형자산 품목이 삭제되었습니다.", null);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PostMapping("/import")
    public ApiResponse<List<TangibleAssetItemResponse>> importItems(
            @AuthenticationPrincipal AuthenticatedMember member,
            @RequestPart("file") MultipartFile file
    ) {
        List<TangibleAssetItemResponse> response =
                tangibleAssetItemService.importItems(file, member.companyId());

        return ApiResponse.ok("유형자산 품목이 일괄 등록되었습니다.", response);
    }
}
