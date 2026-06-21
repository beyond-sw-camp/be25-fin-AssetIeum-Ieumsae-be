package com.ieumsae.assetieum.domain.inspection.followup.controlller;

import com.ieumsae.assetieum.domain.inspection.followup.dto.InspectionFollowUpResponse;
import com.ieumsae.assetieum.domain.inspection.followup.dto.InspectionFollowUpStatusRequest;
import com.ieumsae.assetieum.domain.inspection.followup.service.InspectionFollowUpService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inspections/follow-ups/{followUpId}")
public class InspectionFollowUpController {

    private final InspectionFollowUpService inspectionFollowUpService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
    @PatchMapping("/status")
    public ApiResponse<InspectionFollowUpResponse> updateInspectionFollowUpStatus(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID followUpId,
            @Valid @RequestBody InspectionFollowUpStatusRequest request
    ) {
        InspectionFollowUpResponse response =
                inspectionFollowUpService.updateInspectionFollowUpStatus(followUpId, request, member);

        return ApiResponse.ok("전수조사 후속 처리 상태 변경에 성공했습니다.", response);
    }

    @GetMapping
    public ApiResponse<InspectionFollowUpResponse> getInspectionFollowUp(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID followUpId
    ) {
        InspectionFollowUpResponse response =
                inspectionFollowUpService.getInspectionFollowUp(followUpId, member);

        return ApiResponse.ok("전수조사 후속 처리 조회에 성공했습니다.", response);
    }

}
