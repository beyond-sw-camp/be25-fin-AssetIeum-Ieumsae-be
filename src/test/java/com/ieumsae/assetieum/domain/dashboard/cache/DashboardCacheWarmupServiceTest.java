package com.ieumsae.assetieum.domain.dashboard.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ieumsae.assetieum.domain.dashboard.repository.DashboardRepository;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DashboardCacheWarmupServiceTest {

	@Test
	@SuppressWarnings("unchecked")
	void warmsUpCompanyLevelSummaries() {
		DashboardRepository dashboardRepository = mock(DashboardRepository.class);
		DashboardCacheService cacheService = mock(DashboardCacheService.class);
		DashboardCacheWarmupService service = new DashboardCacheWarmupService(dashboardRepository, cacheService);
		ReflectionTestUtils.setField(service, "warmupEnabled", true);
		UUID companyId = UUID.randomUUID();

		service.warmUpCompanySummaries(companyId);

		verify(cacheService).getOrLoad(eq(companyId), eq("ticket:all"), any(), any(Supplier.class));
		verify(cacheService).getOrLoad(eq(companyId), eq("owned:all"), any(), any(Supplier.class));
		verify(cacheService).getOrLoad(eq(companyId), eq("expiring:all"), any(), any(Supplier.class));
		verify(cacheService, times(3)).getOrLoad(eq(companyId), any(), any(), any(Supplier.class));
	}
}
