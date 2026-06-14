package com.ieumsae.assetieum.domain.tangibleasset.assignment.controller;

import com.ieumsae.assetieum.domain.tangibleasset.assignment.dto.TangibleAssetAssignmentRequest;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.dto.TangibleAssetAssignmentResponse;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.service.TangibleAssetAssignmentService;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/v1/tangible-asset/assets")
public class TangibleAssetAssignmentController {

    private final TangibleAssetAssignmentService tangibleAssetAssignmentService;

    @GetMapping("/{assetId}/assignments")
    public ApiResponse<List<TangibleAssetAssignmentResponse>> getAssignments(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId,
            @RequestParam(required = false) AssignmentStatus assignmentStatus
    ) {
        List<TangibleAssetAssignmentResponse> response =
                tangibleAssetAssignmentService.getAssignments(assetId, assignmentStatus, member.companyId());

        return ApiResponse.ok("유형자산 배정 이력 조회에 성공했습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM')")
    @PostMapping("/{assetId}/assign")
    public ApiResponse<TangibleAssetAssignmentResponse> assignAsset(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId,
            @Valid @RequestBody TangibleAssetAssignmentRequest request
    ) {
        TangibleAssetAssignmentResponse response =
                tangibleAssetAssignmentService.assignAsset(assetId, request, member.companyId());

        return ApiResponse.ok("유형자산이 배정되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM')")
    @PostMapping("/{assetId}/return")
    public ApiResponse<TangibleAssetAssignmentResponse> cancelAsset(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId
    ) {
        TangibleAssetAssignmentResponse response =
                tangibleAssetAssignmentService.cancelAsset(assetId, member.companyId());

        return ApiResponse.ok("유형자산이 해지되었습니다.", response);
    }
}
