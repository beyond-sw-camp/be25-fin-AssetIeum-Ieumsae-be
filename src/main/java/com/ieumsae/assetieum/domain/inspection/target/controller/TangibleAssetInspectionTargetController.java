package com.ieumsae.assetieum.domain.inspection.target.controller;

import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.target.dto.InspectionTargetResponse;
import com.ieumsae.assetieum.domain.inspection.target.dto.InspectionTargetSearchRequest;
import com.ieumsae.assetieum.domain.inspection.target.service.InspectionTargetService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tangible-asset/inspections")
public class TangibleAssetInspectionTargetController {

    private final InspectionTargetService inspectionTargetService;

    @GetMapping("/my-targets")
    public ApiResponse<PaginationResponse<InspectionTargetResponse>> getMyInspectionTargets(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute InspectionTargetSearchRequest request
    ) {
        PaginationResponse<InspectionTargetResponse> response =
                inspectionTargetService.getMyInspectionTargets(
                        request,
                        InspectionType.TANGIBLE_ASSET,
                        member.companyId(),
                        member.id()
                );

        return ApiResponse.ok("사원의 유형자산 전수조사 대상 자산 조회에 성공했습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @GetMapping("/targets")
    public ApiResponse<PaginationResponse<InspectionTargetResponse>> getInspectionTargets(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute InspectionTargetSearchRequest request
    ) {
        PaginationResponse<InspectionTargetResponse> response =
                inspectionTargetService.getInspectionTargets(
                        request,
                        InspectionType.TANGIBLE_ASSET,
                        member.companyId(),
                        member.id()
                );

        return ApiResponse.ok("자산팀의 유형자산 전수조사 대상 자산 조회에 성공했습니다.", response);
    }

}
