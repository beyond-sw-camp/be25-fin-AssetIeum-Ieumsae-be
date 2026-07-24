package com.ieumsae.assetieum.domain.dashboard.cache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DashboardCacheInvalidationConsumerTest {

	@Test
	void invalidatesCompanyCacheFromAuditEvent() {
		DashboardCacheService cacheService = mock(DashboardCacheService.class);
		DashboardCacheWarmupService warmupService = mock(DashboardCacheWarmupService.class);
		DashboardCacheInvalidationConsumer consumer = new DashboardCacheInvalidationConsumer(cacheService, warmupService);
		UUID companyId = UUID.randomUUID();
		when(cacheService.invalidate(companyId)).thenReturn(true);

		consumer.consume(new ObjectMapper().createObjectNode().put("companyId", companyId.toString()));

		verify(cacheService).invalidate(companyId);
		verify(warmupService).warmUpCompanySummaries(companyId);
	}

	@Test
	void skipsWarmupWhenInvalidationFails() {
		DashboardCacheService cacheService = mock(DashboardCacheService.class);
		DashboardCacheWarmupService warmupService = mock(DashboardCacheWarmupService.class);
		DashboardCacheInvalidationConsumer consumer = new DashboardCacheInvalidationConsumer(cacheService, warmupService);
		UUID companyId = UUID.randomUUID();
		when(cacheService.invalidate(companyId)).thenReturn(false);

		consumer.consume(new ObjectMapper().createObjectNode().put("companyId", companyId.toString()));

		verify(warmupService, never()).warmUpCompanySummaries(companyId);
	}
}
