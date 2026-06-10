package com.ieumsae.assetieum.domain.intangibleasset.item.controller;

import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemDeleteResponse;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemSearchRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemUpdateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.service.IntangibleAssetItemService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping
    public ApiResponse<IntangibleAssetItemResponse> createItem(
            @Valid @RequestBody IntangibleAssetItemCreateRequest request
    ) {
        IntangibleAssetItemResponse response =
                intangibleAssetItemService.createItem(request);

        return ApiResponse.ok("무형자산 품목이 등록되었습니다.", response);
    }

    @GetMapping
    public ApiResponse<PaginationResponse<IntangibleAssetItemResponse>> getItems(
            @Valid @ModelAttribute IntangibleAssetItemSearchRequest request
    ) {
        PaginationResponse<IntangibleAssetItemResponse> response =
                intangibleAssetItemService.getItems(request);

        return ApiResponse.ok("무형자산 품목 목록 조회에 성공했습니다.", response);
    }

    @PatchMapping("/{itemId}")
    public ApiResponse<IntangibleAssetItemResponse> updateItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody IntangibleAssetItemUpdateRequest request
    ) {
        IntangibleAssetItemResponse response =
                intangibleAssetItemService.updateItem(itemId, request);

        return ApiResponse.ok("무형자산 품목이 수정되었습니다.", response);
    }

    @DeleteMapping("/{itemId}")
    public ApiResponse<IntangibleAssetItemDeleteResponse> deleteItem(
            @PathVariable UUID itemId
    ) {
        IntangibleAssetItemDeleteResponse response = intangibleAssetItemService.deleteItem(itemId);

        return ApiResponse.ok("무형자산 품목이 삭제되었습니다.", response);
    }
}
