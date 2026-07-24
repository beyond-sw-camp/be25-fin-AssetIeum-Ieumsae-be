package com.ieumsae.assetieum.domain.dashboard.cache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DashboardCacheInvalidationConsumerTest {

	@Test
	void invalidatesCompanyCacheFromAuditEvent() {
		DashboardCacheService cacheService = mock(DashboardCacheService.class);
		DashboardCacheInvalidationConsumer consumer = new DashboardCacheInvalidationConsumer(cacheService);
		UUID companyId = UUID.randomUUID();

		consumer.consume(new ObjectMapper().createObjectNode().put("companyId", companyId.toString()));

		verify(cacheService).invalidate(companyId);
	}
}
