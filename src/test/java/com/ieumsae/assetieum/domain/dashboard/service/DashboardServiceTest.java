package com.ieumsae.assetieum.domain.dashboard.service;

import com.ieumsae.assetieum.domain.dashboard.dto.BudgetLedgerSearchRequest;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Transactional
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Test
    @DisplayName("대시보드 티켓 진행 상태 요약 조회 쿼리 테스트")
    void testGetTicketProgressSummary() {
        UUID companyId = UUID.randomUUID();
        assertThatCode(() -> dashboardService.getTicketProgressSummary(companyId, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("대시보드 보유 자산 요약 조회 쿼리 테스트")
    void testGetOwnedAssetSummary() {
        UUID companyId = UUID.randomUUID();
        assertThatCode(() -> dashboardService.getOwnedAssetSummary(companyId, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("대시보드 만료 예정 자산 요약 조회 쿼리 테스트")
    void testGetExpiringAssetSummary() {
        UUID companyId = UUID.randomUUID();
        assertThatCode(() -> dashboardService.getExpiringAssetSummary(companyId, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("대시보드 자산 수요 조회 쿼리 테스트")
    void testGetAssetDemands() {
        UUID companyId = UUID.randomUUID();
        PaginationRequest request = new PaginationRequest();
        request.setPage(1);
        request.setSize(10);
        assertThatCode(() -> dashboardService.getAssetDemands(companyId, request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("대시보드 예산 개요 조회 쿼리 테스트")
    void testGetBudgetOverview() {
        UUID companyId = UUID.randomUUID();
        PaginationRequest request = new PaginationRequest();
        request.setPage(1);
        request.setSize(10);
        assertThatCode(() -> dashboardService.getBudgetOverview(companyId, request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("대시보드 라이프사이클 이벤트 조회 쿼리 테스트")
    void testGetLifecycleEvents() {
        UUID companyId = UUID.randomUUID();
        PaginationRequest request = new PaginationRequest();
        request.setPage(1);
        request.setSize(10);
        assertThatCode(() -> dashboardService.getLifecycleEvents(companyId, request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("대시보드 예산 장부 조회 쿼리 테스트")
    void testGetBudgetLedger() {
        UUID companyId = UUID.randomUUID();
        BudgetLedgerSearchRequest request = new BudgetLedgerSearchRequest();
        request.setPage(1);
        request.setSize(10);
        assertThatCode(() -> dashboardService.getBudgetLedger(companyId, request))
                .doesNotThrowAnyException();
    }
}
