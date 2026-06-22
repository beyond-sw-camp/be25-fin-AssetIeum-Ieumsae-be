package com.ieumsae.assetieum.domain.log.controller;

import com.ieumsae.assetieum.domain.log.dto.ActivityLogResponse;
import com.ieumsae.assetieum.domain.log.dto.ActivityLogSearchRequest;
import com.ieumsae.assetieum.domain.log.dto.AuditLogResponse;
import com.ieumsae.assetieum.domain.log.dto.AuditLogSearchRequest;
import com.ieumsae.assetieum.domain.log.service.LogService;
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
@RequestMapping("/api/v1")
public class LogController {

	private final LogService logService;

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/audit-logs")
	public ApiResponse<PaginationResponse<AuditLogResponse>> getAuditLogs(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute AuditLogSearchRequest request
	) {
		PaginationResponse<AuditLogResponse> response = logService.getAuditLogs(
			request,
			authenticatedMember.companyId()
		);
		return ApiResponse.ok("감사로그 목록 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/activity-logs")
	public ApiResponse<PaginationResponse<ActivityLogResponse>> getActivityLogs(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute ActivityLogSearchRequest request
	) {
		PaginationResponse<ActivityLogResponse> response = logService.getActivityLogs(
			request,
			authenticatedMember.companyId()
		);
		return ApiResponse.ok("활동로그 목록 조회에 성공했습니다.", response);
	}
}
