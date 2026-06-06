package com.ieumsae.assetieum.domain.tangibleasset.item.controller;

import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.tangibleasset.item.service.TangibleAssetItemService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tangible-asset/items")
public class TangibleAssetItemController {
    private final TangibleAssetItemService tangibleAssetItemService;

    @PostMapping
    public ApiResponse<TangibleAssetItemResponse> createItem(
            @Valid @RequestBody TangibleAssetItemCreateRequest request
    ) {
        TangibleAssetItemResponse response =
                tangibleAssetItemService.createItem(request);

        return ApiResponse.created("유형자산 품목이 등록되었습니다.", response);
    }
}
