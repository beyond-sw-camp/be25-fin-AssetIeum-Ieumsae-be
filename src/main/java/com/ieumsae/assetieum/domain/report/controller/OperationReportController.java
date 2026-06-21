package com.ieumsae.assetieum.domain.report.controller;

import com.ieumsae.assetieum.domain.report.dto.OperationReportPageRequest;
import com.ieumsae.assetieum.domain.report.dto.OperationReportPeriodRequest;
import com.ieumsae.assetieum.domain.report.dto.PurchaseOperationReportResponse;
import com.ieumsae.assetieum.domain.report.dto.RecoveryOperationReportResponse;
import com.ieumsae.assetieum.domain.report.dto.UnreturnedAssetReportResponse;
import com.ieumsae.assetieum.domain.report.service.OperationReportService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/reports/operations")
public class OperationReportController {

	private final OperationReportService operationReportService;

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/unreturned-assets")
	public ApiResponse<UnreturnedAssetReportResponse> getUnreturnedAssetReport(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@RequestParam(required = false, defaultValue = "3") Integer topDelayedUserLimit
	) {
		return ApiResponse.ok(
			"미반납 및 지연 자산 리포트 조회에 성공했습니다.",
			operationReportService.getUnreturnedAssetReport(authenticatedMember.companyId(), topDelayedUserLimit)
		);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/recovery")
	public ApiResponse<RecoveryOperationReportResponse> getRecoveryOperationReport(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute OperationReportPeriodRequest request
	) {
		return ApiResponse.ok(
			"회수 요청 및 처리 현황 리포트 조회에 성공했습니다.",
			operationReportService.getRecoveryOperationReport(authenticatedMember.companyId(), request)
		);
	}

	@PreAuthorize("hasAnyRole('ASSET_MANAGER', 'ASSET_TEAM', 'ADMIN')")
	@GetMapping("/purchase-requests")
	public ApiResponse<PurchaseOperationReportResponse> getPurchaseOperationReport(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute OperationReportPageRequest request
	) {
		return ApiResponse.ok(
			"신규 구매 및 부서별 구매 요청 현황 조회에 성공했습니다.",
			operationReportService.getPurchaseOperationReport(authenticatedMember.companyId(), request)
		);
	}
}
