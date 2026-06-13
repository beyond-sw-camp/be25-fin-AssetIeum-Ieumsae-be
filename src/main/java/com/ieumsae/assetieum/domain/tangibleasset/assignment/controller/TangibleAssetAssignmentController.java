package com.ieumsae.assetieum.domain.tangibleasset.assignment.controller;

import com.ieumsae.assetieum.domain.tangibleasset.assignment.dto.TangibleAssetAssignmentSearchResponse;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.service.TangibleAssetAssignmentService;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tangible-asset/assets")
public class TangibleAssetAssignmentController {

    private final TangibleAssetAssignmentService tangibleAssetAssignmentService;

    @GetMapping("/{assetId}/assignments")
    public ApiResponse<List<TangibleAssetAssignmentSearchResponse>> getAssignments(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId,
            @RequestParam(required = false) AssignmentStatus assignmentStatus
    ) {
        List<TangibleAssetAssignmentSearchResponse> response =
                tangibleAssetAssignmentService.getAssignments(assetId, assignmentStatus, member.companyId());

        return ApiResponse.ok("유형자산 배정 이력 조회에 성공했습니다.", response);
    }
}
