package com.ieumsae.assetieum.domain.inspection.inspection.controller;

import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionCreateRequest;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionResponse;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionSearchRequest;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionSearchResponse;
import com.ieumsae.assetieum.domain.inspection.inspection.service.InspectionService;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tangible-asset/inspections")
public class TangibleAssetInspectionController {

    private final InspectionService inspectionService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PostMapping
    public ApiResponse<InspectionResponse> createInspection(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody InspectionCreateRequest request
    ) {
        InspectionResponse response =
                inspectionService.createInspection(request, InspectionType.TANGIBLE_ASSET, member.companyId());

        return ApiResponse.ok("유형자산 전수조사 계획이 등록되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @GetMapping
    public ApiResponse<PaginationResponse<InspectionSearchResponse>> getInspections(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute InspectionSearchRequest request
    ) {
        PaginationResponse<InspectionSearchResponse> response =
                inspectionService.getInspections(request, InspectionType.TANGIBLE_ASSET, member.companyId());

        return ApiResponse.ok("유형자산 전수조사 목록이 조회되었습니다.", response);
    }

}