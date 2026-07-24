package com.ieumsae.assetieum.domain.dashboard.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardCacheService {

	private static final String PREFIX = "assetieum:dashboard:";

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final MeterRegistry meterRegistry;

	@Value("${app.kafka.dashboard-cache.enabled:false}")
	private boolean enabled;

	@Value("${app.kafka.dashboard-cache.ttl-seconds:30}")
	private long ttlSeconds;

	public <T> T getOrLoad(UUID companyId, String queryKey, Class<T> type, Supplier<T> loader) {
		if (!enabled) {
			return loader.get();
		}
		String cacheKey;
		try {
			cacheKey = cacheKey(companyId, queryKey, currentVersion(companyId));
			String cached = redisTemplate.opsForValue().get(cacheKey);
			if (cached != null) {
				count("hit", queryKey);
				return objectMapper.readValue(cached, type);
			}
			count("miss", queryKey);
		} catch (JsonProcessingException | RuntimeException exception) {
			count("error", queryKey);
			log.warn("Dashboard cache read failed; falling back to database. companyId={}, query={}",
				companyId, queryKey, exception);
			return loader.get();
		}

		T value = loader.get();
		try {
			redisTemplate.opsForValue().set(
				cacheKey, objectMapper.writeValueAsString(value), Duration.ofSeconds(ttlSeconds)
			);
		} catch (JsonProcessingException | RuntimeException exception) {
			count("error", queryKey);
			log.warn("Dashboard cache write failed. companyId={}, query={}",
				companyId, queryKey, exception);
		}
		return value;
	}

	public void invalidate(UUID companyId) {
		if (!enabled || companyId == null) {
			return;
		}
		try {
			redisTemplate.opsForValue().increment(versionKey(companyId));
			meterRegistry.counter("dashboard.cache.invalidation.total").increment();
		} catch (RuntimeException exception) {
			log.warn("Dashboard cache invalidation failed. companyId={}", companyId, exception);
		}
	}

	private long currentVersion(UUID companyId) {
		String version = redisTemplate.opsForValue().get(versionKey(companyId));
		return version == null ? 0L : Long.parseLong(version);
	}

	private String versionKey(UUID companyId) {
		return PREFIX + companyId + ":version";
	}

	private String cacheKey(UUID companyId, String queryKey, long version) {
		return PREFIX + companyId + ":v" + version + ":" + queryKey;
	}

	private void count(String result, String query) {
		meterRegistry.counter("dashboard.cache.requests.total", "result", result, "query", query).increment();
	}
}
