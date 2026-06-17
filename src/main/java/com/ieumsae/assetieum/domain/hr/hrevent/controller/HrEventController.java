package com.ieumsae.assetieum.domain.hr.hrevent.controller;

import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventCreateRequest;
import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventResponse;
import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventSearchRequest;
import com.ieumsae.assetieum.domain.hr.hrevent.service.HrEventService;
import com.ieumsae.assetieum.domain.hr.hrtemplate.dto.HrTemplateResponse;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hr-events")
public class HrEventController {

    private final HrEventService hrEventService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'DEPARTMENT_MANAGER')")
    @PostMapping
    public ApiResponse<HrEventResponse> createHrEvent(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody HrEventCreateRequest request
    ) {
        HrEventResponse response =
                hrEventService.createHrEvent(request, member);

        return ApiResponse.ok("HR 이벤트가 등록되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'DEPARTMENT_MANAGER')")
    @DeleteMapping("/{eventId}")
    public ApiResponse<HrTemplateResponse> deleteHrTemplate(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID eventId
    ) {
        HrTemplateResponse response =
                hrEventService.deleteHrEvent(eventId, member);

        return ApiResponse.ok("HR 이벤트가 삭제되었습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'DEPARTMENT_MANAGER')")
    @GetMapping
    public ApiResponse<PaginationResponse<HrEventResponse>> getHrEvents(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @ModelAttribute HrEventSearchRequest request
    ) {
        PaginationResponse<HrEventResponse> response =
                hrEventService.getHrEvents(request, member);

        return ApiResponse.ok("HR 이벤트 목록 조회에 성공했습니다.", response);
    }

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'DEPARTMENT_MANAGER')")
    @PatchMapping("/{eventId}/complete")
    public ApiResponse<HrEventResponse> completeHrEvent(
            @AuthenticationPrincipal AuthenticatedMember member,
            @PathVariable UUID eventId
    ) {
        HrEventResponse response =
                hrEventService.completeHrEvent(eventId, member);

        return ApiResponse.ok("HR 이벤트가 완료 처리되었습니다.", response);
    }

}
