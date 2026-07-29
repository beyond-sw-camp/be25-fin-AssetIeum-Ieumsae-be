package com.ieumsae.assetieum.domain.dashboard.cache;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.kafka.dashboard-cache", name = "enabled", havingValue = "true")
public class DashboardCacheInvalidationConsumer {

	private final DashboardCacheService dashboardCacheService;
	private final DashboardCacheWarmupService dashboardCacheWarmupService;

	@KafkaListener(
		topics = "${app.kafka.topics.audit-log}",
		groupId = "${KAFKA_DASHBOARD_CONSUMER_GROUP_ID:assetieum-dashboard-cache}"
	)
	public void consume(JsonNode event) {
		JsonNode companyId = event.get("companyId");
		if (companyId != null && !companyId.isNull()) {
			UUID id = UUID.fromString(companyId.asText());
			if (dashboardCacheService.invalidate(id)) {
				dashboardCacheWarmupService.warmUpCompanySummaries(id);
			}
		}
	}
}
