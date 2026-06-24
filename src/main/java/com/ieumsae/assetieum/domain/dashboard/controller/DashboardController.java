package com.ieumsae.assetieum.domain.dashboard.controller;

import com.ieumsae.assetieum.domain.dashboard.dto.AssetDemandResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetLedgerResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetLedgerSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetOverviewResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetDetailResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetDetailSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.LifecycleEventResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetDetailResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetDetailSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.TicketProgressSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.service.DashboardService;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
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
package com.ieumsae.assetieum.domain.dashboard.controller;

import com.ieumsae.assetieum.domain.dashboard.dto.AssetDemandResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetLedgerResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetLedgerSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetOverviewResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetDetailResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetDetailSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.LifecycleEventResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetDetailResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetDetailSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.TicketProgressSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.service.DashboardService;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
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
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN', 'DEPARTMENT_MANAGER')")
	@GetMapping("/ticket-progress")
	public ApiResponse<TicketProgressSummaryResponse> getTicketProgressSummary(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@RequestParam(required = false) UUID departmentId
	) {
		TicketProgressSummaryResponse response = isEmployee(authenticatedMember)
			? dashboardService.getEmployeeTicketProgressSummary(authenticatedMember.companyId(), authenticatedMember.id())
			: isDepartmentManager(authenticatedMember)
			? dashboardService.getDepartmentTicketProgressSummary(authenticatedMember.companyId(), authenticatedMember.id())
			: dashboardService.getTicketProgressSummary(authenticatedMember.companyId(), departmentId);

		return ApiResponse.ok("진행중인 티켓 현황 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN', 'DEPARTMENT_MANAGER')")
	@GetMapping("/owned-assets")
	public ApiResponse<OwnedAssetSummaryResponse> getOwnedAssetSummary(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@RequestParam(required = false) UUID departmentId
	) {
		OwnedAssetSummaryResponse response = isEmployee(authenticatedMember)
			? dashboardService.getEmployeeOwnedAssetSummary(authenticatedMember.companyId(), authenticatedMember.id())
			: isDepartmentManager(authenticatedMember)
			? dashboardService.getDepartmentOwnedAssetSummary(authenticatedMember.companyId(), authenticatedMember.id())
			: dashboardService.getOwnedAssetSummary(authenticatedMember.companyId(), departmentId);

		return ApiResponse.ok("보유자산 현황 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN', 'DEPARTMENT_MANAGER')")
	@GetMapping("/owned-assets/details")
	public ApiResponse<PaginationResponse<OwnedAssetDetailResponse>> getOwnedAssetDetails(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute OwnedAssetDetailSearchRequest request
	) {
		PaginationResponse<OwnedAssetDetailResponse> response = isEmployee(authenticatedMember)
			? dashboardService.getEmployeeOwnedAssetDetails(authenticatedMember.companyId(), authenticatedMember.id(), request)
			: isDepartmentManager(authenticatedMember)
			? dashboardService.getDepartmentOwnedAssetDetails(authenticatedMember.companyId(), authenticatedMember.id(), request)
			: dashboardService.getOwnedAssetDetails(authenticatedMember.companyId(), request);
		@RequestParam(required = false) UUID departmentId
	) {
		ExpiringAssetSummaryResponse response = isEmployee(authenticatedMember)
			? dashboardService.getEmployeeExpiringAssetSummary(authenticatedMember.companyId(), authenticatedMember.id())
			: dashboardService.getExpiringAssetSummary(authenticatedMember.companyId(), departmentId);

		return ApiResponse.ok("만료예정 자산 현황 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN', 'DEPARTMENT_MANAGER')")
	@GetMapping("/expiring-assets/details")
	public ApiResponse<PaginationResponse<ExpiringAssetDetailResponse>> getExpiringAssetDetails(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute ExpiringAssetDetailSearchRequest request
	) {
		PaginationResponse<ExpiringAssetDetailResponse> response = isEmployee(authenticatedMember)
			? dashboardService.getEmployeeExpiringAssetDetails(authenticatedMember.companyId(), authenticatedMember.id(), request)
			: isDepartmentManager(authenticatedMember)
			? dashboardService.getDepartmentExpiringAssetDetails(authenticatedMember.companyId(), authenticatedMember.id(), request)
			: dashboardService.getExpiringAssetDetails(authenticatedMember.companyId(), request);

		return ApiResponse.ok("만료예정 자산 현황 상세 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN', 'DEPARTMENT_MANAGER')")
	@GetMapping("/asset-demands")
	public ApiResponse<PaginationResponse<AssetDemandResponse>> getAssetDemands(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute PaginationRequest request
	) {
		PaginationResponse<AssetDemandResponse> response = isEmployee(authenticatedMember)
			? dashboardService.getEmployeeDepartmentAssetDemands(
				authenticatedMember.companyId(),
				authenticatedMember.id(),
				request
			)
			: dashboardService.getAssetDemands(authenticatedMember.companyId(), request);

		return ApiResponse.ok("자산 수요 정보 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN', 'DEPARTMENT_MANAGER')")
	@GetMapping("/budgets")
	public ApiResponse<BudgetOverviewResponse> getBudgetOverview(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute PaginationRequest request
	) {
		BudgetOverviewResponse response = isEmployee(authenticatedMember)
			? dashboardService.getEmployeeBudgetOverview(
				authenticatedMember.companyId(),
				authenticatedMember.id(),
				request
			)
			: dashboardService.getBudgetOverview(authenticatedMember.companyId(), request);

		return ApiResponse.ok("부서별 예산 현황 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN', 'DEPARTMENT_MANAGER')")
	@GetMapping("/lifecycle-events")
	public ApiResponse<PaginationResponse<LifecycleEventResponse>> getLifecycleEvents(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute PaginationRequest request
	) {
		return ApiResponse.ok(
			"라이프 사이클 진행현황 조회에 성공했습니다.",
			dashboardService.getLifecycleEvents(authenticatedMember.companyId(), request)
		);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN', 'DEPARTMENT_MANAGER')")
	@GetMapping("/budget-ledger")
	public ApiResponse<PaginationResponse<BudgetLedgerResponse>> getBudgetLedger(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute BudgetLedgerSearchRequest request
	) {
		return ApiResponse.ok(
			"예산 집행 이력 장부 조회에 성공했습니다.",
			dashboardService.getBudgetLedger(authenticatedMember.companyId(), request)
		);
	}

	private boolean isEmployee(AuthenticatedMember authenticatedMember) {
		return authenticatedMember.role() == MemberRole.EMPLOYEE;
	}

	private boolean isDepartmentManager(AuthenticatedMember authenticatedMember) {
		return authenticatedMember.role() == MemberRole.DEPARTMENT_MANAGER;
	}
}
