package com.ieumsae.assetieum.domain.dashboard.controller;

import com.ieumsae.assetieum.domain.dashboard.dto.AssetDemandResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetLedgerResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetLedgerSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetOverviewResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.LifecycleEventResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.TicketProgressSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.service.DashboardService;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/ticket-progress")
	public ApiResponse<TicketProgressSummaryResponse> getTicketProgressSummary(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		return ApiResponse.ok(
			"진행중인 티켓 현황 조회에 성공했습니다.",
			dashboardService.getTicketProgressSummary(authenticatedMember.companyId())
		);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/owned-assets")
	public ApiResponse<OwnedAssetSummaryResponse> getOwnedAssetSummary(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		return ApiResponse.ok(
			"보유자산 현황 조회에 성공했습니다.",
			dashboardService.getOwnedAssetSummary(authenticatedMember.companyId())
		);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/expiring-assets")
	public ApiResponse<ExpiringAssetSummaryResponse> getExpiringAssetSummary(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		return ApiResponse.ok(
			"만료예정 자산 현황 조회에 성공했습니다.",
			dashboardService.getExpiringAssetSummary(authenticatedMember.companyId())
		);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/asset-demands")
	public ApiResponse<PaginationResponse<AssetDemandResponse>> getAssetDemands(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute PaginationRequest request
	) {
		return ApiResponse.ok(
			"자산 수요 정보 조회에 성공했습니다.",
			dashboardService.getAssetDemands(authenticatedMember.companyId(), request)
		);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/budgets")
	public ApiResponse<BudgetOverviewResponse> getBudgetOverview(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute PaginationRequest request
	) {
		return ApiResponse.ok(
			"부서별 예산 현황 조회에 성공했습니다.",
			dashboardService.getBudgetOverview(authenticatedMember.companyId(), request)
		);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
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

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
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
}
