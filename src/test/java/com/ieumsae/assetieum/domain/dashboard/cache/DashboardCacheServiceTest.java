package com.ieumsae.assetieum.domain.dashboard.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.dashboard.dto.TicketProgressSummaryResponse;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

class DashboardCacheServiceTest {

	private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
	@SuppressWarnings("unchecked")
	private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
	private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
	private final DashboardCacheService service = new DashboardCacheService(
		redisTemplate, new ObjectMapper(), meterRegistry
	);

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(service, "enabled", true);
		ReflectionTestUtils.setField(service, "ttlSeconds", 30L);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	void returnsCachedSummaryWithoutCallingDatabase() {
		UUID companyId = UUID.randomUUID();
		when(valueOperations.get("assetieum:dashboard:" + companyId + ":version")).thenReturn("2");
		when(valueOperations.get("assetieum:dashboard:" + companyId + ":v2:ticket:all"))
			.thenReturn("{\"waitingReceipt\":1,\"receiptCompleted\":2,\"processing\":3,\"completed\":4}");
		AtomicInteger databaseCalls = new AtomicInteger();

		TicketProgressSummaryResponse result = service.getOrLoad(
			companyId, "ticket:all", TicketProgressSummaryResponse.class,
			() -> {
				databaseCalls.incrementAndGet();
				return null;
			}
		);

		assertThat(result.getCompleted()).isEqualTo(4);
		assertThat(databaseCalls).hasValue(0);
	}

	@Test
	void cachesDatabaseResultOnMiss() {
		UUID companyId = UUID.randomUUID();
		when(valueOperations.get("assetieum:dashboard:" + companyId + ":version")).thenReturn(null);
		TicketProgressSummaryResponse databaseResult = TicketProgressSummaryResponse.builder()
			.waitingReceipt(1).receiptCompleted(2).processing(3).completed(4).build();

		TicketProgressSummaryResponse result = service.getOrLoad(
			companyId, "ticket:all", TicketProgressSummaryResponse.class, () -> databaseResult
		);

		assertThat(result).isSameAs(databaseResult);
		verify(valueOperations).set(
			eq("assetieum:dashboard:" + companyId + ":v0:ticket:all"),
			any(String.class),
			eq(Duration.ofSeconds(30))
		);
	}

	@Test
	void bypassesRedisWhenFeatureIsDisabled() {
		ReflectionTestUtils.setField(service, "enabled", false);
		TicketProgressSummaryResponse databaseResult = TicketProgressSummaryResponse.builder().build();

		assertThat(service.getOrLoad(
			UUID.randomUUID(), "ticket:all", TicketProgressSummaryResponse.class, () -> databaseResult
		)).isSameAs(databaseResult);

		verify(redisTemplate, never()).opsForValue();
	}
}
