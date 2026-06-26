package com.ieumsae.assetieum.domain.intangibleasset.assignment.controller;

import com.ieumsae.assetieum.domain.intangibleasset.assignment.dto.IntangibleAssetCancelRequest;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.dto.IntangibleAssetAssignmentRequest;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.dto.IntangibleAssetAssignmentResponse;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.service.IntangibleAssetAssignmentService;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
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
@RequestMapping("/api/v1/intangible-asset/assets")
public class IntangibleAssetAssignmentController {

    private final IntangibleAssetAssignmentService intangibleAssetAssignmentService;

    @GetMapping("/{assetId}/assignments")
    public ApiResponse<List<IntangibleAssetAssignmentResponse>> getAssignments(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId,
            @RequestParam(required = false) AssignmentStatus assignmentStatus
    ) {
        List<IntangibleAssetAssignmentResponse> response =
                intangibleAssetAssignmentService.getAssignments(assetId, assignmentStatus, member.companyId());

        return ApiResponse.ok("무형자산 배정 이력 조회에 성공했습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PostMapping("/{assetId}/assign")
    public ApiResponse<IntangibleAssetAssignmentResponse> assignAsset(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId,
            @Valid @RequestBody IntangibleAssetAssignmentRequest request
    ) {
        IntangibleAssetAssignmentResponse response =
                intangibleAssetAssignmentService.assignAsset(assetId, request, member.companyId());

        return ApiResponse.ok("무형자산이 배정되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PostMapping("/{assetId}/cancel")
    public ApiResponse<List<IntangibleAssetAssignmentResponse>> cancelAsset(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId,
            @RequestBody(required = false) IntangibleAssetCancelRequest request
    ) {
        UUID memberId = request == null ? null : request.getMemberId();
        List<IntangibleAssetAssignmentResponse> response =
                intangibleAssetAssignmentService.cancelAsset(assetId, memberId, member.companyId());

        return ApiResponse.ok("무형자산이 해지되었습니다.", response);
    }

}
