package com.ieumsae.assetieum.domain.dashboard.controller;

import com.ieumsae.assetieum.domain.dashboard.dto.DepartmentBudgetDetailResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.HrEventStatisticsResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.HrLifecycleEventResponse;
import com.ieumsae.assetieum.domain.dashboard.service.DepartmentDashboardService;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/department-dashboard")
public class DepartmentDashboardController {

	private final DepartmentDashboardService departmentDashboardService;

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN', 'DEPARTMENT_MANAGER')")
	@GetMapping("/budget-details")
	public ApiResponse<DepartmentBudgetDetailResponse> getDepartmentBudgetDetails(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@RequestParam UUID departmentId,
		@RequestParam(required = false) Integer year
	) {
		return ApiResponse.ok(
			"부서 예산 상세 현황 조회에 성공했습니다.",
			departmentDashboardService.getDepartmentBudgetDetails(authenticatedMember.companyId(), departmentId, year)
		);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN', 'DEPARTMENT_MANAGER')")
	@GetMapping("/hr-events")
	public ApiResponse<PaginationResponse<HrLifecycleEventResponse>> getHrLifecycleEvents(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@RequestParam(required = false) UUID departmentId,
		@RequestParam(required = false) String eventType,
		@Valid @ModelAttribute PaginationRequest request
	) {
		return ApiResponse.ok(
			"HR 인사 이벤트 조회에 성공했습니다.",
			departmentDashboardService.getHrLifecycleEvents(authenticatedMember.companyId(), departmentId, eventType, request)
		);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN', 'DEPARTMENT_MANAGER')")
	@GetMapping("/hr-events/statistics")
	public ApiResponse<HrEventStatisticsResponse> getHrEventStatistics(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@RequestParam(required = false) UUID departmentId
	) {
		return ApiResponse.ok(
			"HR 인사 이벤트 통계 조회에 성공했습니다.",
			departmentDashboardService.getHrEventStatistics(authenticatedMember.companyId(), departmentId)
		);
	}
}
