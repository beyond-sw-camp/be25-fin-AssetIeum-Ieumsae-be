package com.ieumsae.assetieum.domain.inspection.result.controller;

import com.ieumsae.assetieum.domain.inspection.result.dto.InspectionResultCreateRequest;
import com.ieumsae.assetieum.domain.inspection.result.dto.InspectionResultResponse;
import com.ieumsae.assetieum.domain.inspection.result.service.InspectionResultService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inspections/targets/{targetId}/result")
public class InspectionResultController {

    private final InspectionResultService inspectionResultService;

    @PostMapping
    public ApiResponse<InspectionResultResponse> createInspectionResult(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID targetId,
            @Valid @RequestBody InspectionResultCreateRequest request
    ) {
        InspectionResultResponse response =
                inspectionResultService.createInspectionResult(targetId, request, member);

        return ApiResponse.ok("전수조사 응답을 등록했습니다.", response);
    }

    @GetMapping
    public ApiResponse<InspectionResultResponse> getInspectionResult(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID targetId
    ) {
        InspectionResultResponse response =
                inspectionResultService.getInspectionResult(targetId, member);

        return ApiResponse.ok("전수조사 응답 조회에 성공했습니다.", response);
    }
}
