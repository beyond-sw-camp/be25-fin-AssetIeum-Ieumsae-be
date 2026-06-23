package com.ieumsae.assetieum.domain.dashboard.service;

import com.ieumsae.assetieum.domain.dashboard.dto.AssetDemandResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetLedgerResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetLedgerSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.BudgetOverviewResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.EmployeeDepartmentBudgetResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetDetailResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetDetailSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.ExpiringAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.LifecycleEventResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetDetailResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetDetailSearchRequest;
import com.ieumsae.assetieum.domain.dashboard.dto.OwnedAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.RentalAssetSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.dto.TicketProgressSummaryResponse;
import com.ieumsae.assetieum.domain.dashboard.repository.DashboardRepository;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

	private final DashboardRepository dashboardRepository;

	public TicketProgressSummaryResponse getTicketProgressSummary(UUID companyId, UUID departmentId) {
		return dashboardRepository.getTicketProgressSummary(companyId, departmentId);
	}

	public TicketProgressSummaryResponse getDepartmentTicketProgressSummary(UUID companyId, UUID memberId) {
		return dashboardRepository.getDepartmentTicketProgressSummary(companyId, memberId);
	}

	public OwnedAssetSummaryResponse getOwnedAssetSummary(UUID companyId, UUID departmentId) {
		return dashboardRepository.getOwnedAssetSummary(companyId, departmentId);
	}

	public OwnedAssetSummaryResponse getDepartmentOwnedAssetSummary(UUID companyId, UUID memberId) {
		return dashboardRepository.getDepartmentOwnedAssetSummary(companyId, memberId);
	}

	public ExpiringAssetSummaryResponse getExpiringAssetSummary(UUID companyId, UUID departmentId) {
		return dashboardRepository.getExpiringAssetSummary(companyId, departmentId);
	}

	public ExpiringAssetSummaryResponse getDepartmentExpiringAssetSummary(UUID companyId, UUID memberId) {
		return dashboardRepository.getDepartmentExpiringAssetSummary(companyId, memberId);
	}

	public TicketProgressSummaryResponse getEmployeeTicketProgressSummary(UUID companyId, UUID memberId) {
		return dashboardRepository.getEmployeeTicketProgressSummary(companyId, memberId);
	}

	public RentalAssetSummaryResponse getEmployeeRentalAssetSummary(UUID companyId, UUID memberId) {
		return dashboardRepository.getEmployeeRentalAssetSummary(companyId, memberId);
	}

	public OwnedAssetSummaryResponse getEmployeeOwnedAssetSummary(UUID companyId, UUID memberId) {
		return dashboardRepository.getEmployeeOwnedAssetSummary(companyId, memberId);
	}

	public ExpiringAssetSummaryResponse getEmployeeExpiringAssetSummary(UUID companyId, UUID memberId) {
		return dashboardRepository.getEmployeeExpiringAssetSummary(companyId, memberId);
	}

	public PaginationResponse<OwnedAssetDetailResponse> getOwnedAssetDetails(
		UUID companyId,
		OwnedAssetDetailSearchRequest request
	) {
		return dashboardRepository.getOwnedAssetDetails(companyId, request);
	}

	public PaginationResponse<OwnedAssetDetailResponse> getEmployeeOwnedAssetDetails(
		UUID companyId,
		UUID memberId,
		OwnedAssetDetailSearchRequest request
	) {
		return dashboardRepository.getEmployeeOwnedAssetDetails(companyId, memberId, request);
	}

	public PaginationResponse<ExpiringAssetDetailResponse> getExpiringAssetDetails(
		UUID companyId,
		ExpiringAssetDetailSearchRequest request
	) {
		return dashboardRepository.getExpiringAssetDetails(companyId, request);
	}

	public PaginationResponse<ExpiringAssetDetailResponse> getEmployeeExpiringAssetDetails(
		UUID companyId,
		UUID memberId,
		ExpiringAssetDetailSearchRequest request
	) {
		return dashboardRepository.getEmployeeExpiringAssetDetails(companyId, memberId, request);
	}

	public PaginationResponse<AssetDemandResponse> getEmployeeDepartmentAssetDemands(
		UUID companyId,
		UUID memberId,
		PaginationRequest request
	) {
		return dashboardRepository.getEmployeeDepartmentAssetDemands(companyId, memberId, request);
	}

	public EmployeeDepartmentBudgetResponse getEmployeeDepartmentBudget(UUID companyId, UUID memberId) {
		return dashboardRepository.getEmployeeDepartmentBudget(companyId, memberId);
	}

	public BudgetOverviewResponse getEmployeeBudgetOverview(
		UUID companyId,
		UUID memberId,
		PaginationRequest request
	) {
		return dashboardRepository.getEmployeeBudgetOverview(companyId, memberId, request);
	}

	public PaginationResponse<AssetDemandResponse> getAssetDemands(UUID companyId, PaginationRequest request) {
		return dashboardRepository.getAssetDemands(companyId, request);
	}

	public BudgetOverviewResponse getBudgetOverview(UUID companyId, PaginationRequest request) {
		return dashboardRepository.getBudgetOverview(companyId, request);
	}

	public PaginationResponse<LifecycleEventResponse> getLifecycleEvents(UUID companyId, PaginationRequest request) {
		return dashboardRepository.getLifecycleEvents(companyId, request);
	}

	public PaginationResponse<BudgetLedgerResponse> getBudgetLedger(
		UUID companyId,
		BudgetLedgerSearchRequest request
	) {
		return dashboardRepository.getBudgetLedger(companyId, request);
	}
}
