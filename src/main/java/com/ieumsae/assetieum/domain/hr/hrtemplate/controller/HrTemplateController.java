package com.ieumsae.assetieum.domain.hr.hrtemplate.controller;

import com.ieumsae.assetieum.domain.hr.hrtemplate.dto.HrTemplateCreateRequest;
import com.ieumsae.assetieum.domain.hr.hrtemplate.dto.HrTemplateResponse;
import com.ieumsae.assetieum.domain.hr.hrtemplate.service.HrTemplateService;
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
@RequestMapping("/api/v1/hr-templates")
public class HrTemplateController {

    private final HrTemplateService hrTemplateService;

    @PreAuthorize("hasAnyRole('ASSET_MANAGER', 'DEPARTMENT_MANAGER')")
    @PostMapping
    public ApiResponse<HrTemplateResponse> createHrTemplate(
            @AuthenticationPrincipal AuthenticatedMember member,
            @Valid @RequestBody HrTemplateCreateRequest request
    ) {
        HrTemplateResponse response =
                hrTemplateService.createHrTemplate(request, member);

        return ApiResponse.ok("HR 템플릿이 등록되었습니다.", response);
    }
}
