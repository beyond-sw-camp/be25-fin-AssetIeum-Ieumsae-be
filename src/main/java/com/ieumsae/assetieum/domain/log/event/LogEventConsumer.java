package com.ieumsae.assetieum.domain.log.event;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.log.service.LogService;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.kafka.log", name = "enabled", havingValue = "true")
public class LogEventConsumer {

	private final ObjectMapper objectMapper;
	private final LogService logService;

	@KafkaListener(
		topics = "${app.kafka.topics.activity-log}",
		groupId = "${spring.kafka.consumer.group-id}-activity-log"
	)
	public void consumeActivityLog(JsonNode message) {
		EventEnvelope<ActivityLogEvent> event = convert(message, ActivityLogEvent.class);
		logService.persistActivityLogEvent(event.eventId(), event.companyId(), event.payload());
	}

	@KafkaListener(
		topics = "${app.kafka.topics.audit-log}",
		groupId = "${spring.kafka.consumer.group-id}-audit-log"
	)
	public void consumeAuditLog(JsonNode message) {
		EventEnvelope<AuditLogEvent> event = convert(message, AuditLogEvent.class);
		logService.persistAuditLogEvent(event.eventId(), event.companyId(), event.payload());
	}

	private <T> EventEnvelope<T> convert(JsonNode message, Class<T> payloadType) {
		JavaType eventType = objectMapper.getTypeFactory()
			.constructParametricType(EventEnvelope.class, payloadType);
		return objectMapper.convertValue(message, eventType);
	}
}
