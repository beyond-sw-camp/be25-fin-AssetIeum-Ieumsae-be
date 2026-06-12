package com.ieumsae.assetieum.domain.intangibleasset.asset.controller;

import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.service.IntangibleAssetService;
import com.ieumsae.assetieum.domain.intangibleasset.item.service.IntangibleAssetItemService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/intangible-asset/assets")
public class IntangibleAssetController {

    private final IntangibleAssetService intangibleAssetService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM')")
    @PostMapping
    public ApiResponse<IntangibleAssetResponse> createItem(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody IntangibleAssetCreateRequest request
    ) {
        IntangibleAssetResponse response =
                intangibleAssetService.createAsset(request, member.companyId());

        return ApiResponse.ok("무형자산이 등록되었습니다.", response);
    }
}
