package com.ieumsae.assetieum.domain.notification.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.kafka.notification", name = "enabled", havingValue = "true")
public class NotificationEventConsumer {

	private final ObjectMapper objectMapper;
	private final NotificationService notificationService;

	@KafkaListener(topics = "${app.kafka.topics.notification}")
	public void consume(JsonNode message) {
		EventEnvelope<NotificationCreatedEvent> event = objectMapper.convertValue(
			message,
			new TypeReference<>() {
			}
		);
		notificationService.persistNotificationEvent(event.eventId(), event.companyId(), event.payload());
	}
}
