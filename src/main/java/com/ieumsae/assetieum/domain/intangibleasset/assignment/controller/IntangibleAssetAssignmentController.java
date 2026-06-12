package com.ieumsae.assetieum.domain.intangibleasset.assignment.controller;

import com.ieumsae.assetieum.domain.intangibleasset.assignment.dto.IntangibleAssetAssignmentSearchResponse;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.service.IntangibleAssetAssignmentService;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
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
@RequestMapping("/api/v1/intangible-asset/assets")
public class IntangibleAssetAssignmentController {

    private final IntangibleAssetAssignmentService intangibleAssetAssignmentService;

    @GetMapping("/{assetId}/assignments")
    public ApiResponse<List<IntangibleAssetAssignmentSearchResponse>> getAssignments(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID assetId,
            @RequestParam(required = false) AssignmentStatus assignmentStatus
    ) {
        List<IntangibleAssetAssignmentSearchResponse> response =
                intangibleAssetAssignmentService.getAssignments(assetId, assignmentStatus, member.companyId());

        return ApiResponse.ok("무형자산 배정 이력 조회에 성공했습니다.", response);
    }
}
