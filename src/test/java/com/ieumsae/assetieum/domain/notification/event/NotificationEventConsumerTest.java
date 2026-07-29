package com.ieumsae.assetieum.domain.notification.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationEventConsumerTest {

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
	private final NotificationService notificationService = mock(NotificationService.class);
	private final NotificationEventConsumer consumer = new NotificationEventConsumer(
		objectMapper, notificationService
	);

	@Test
	void convertsEnvelopeAndPersistsNotification() {
		UUID companyId = UUID.randomUUID();
		NotificationCreatedEvent payload = new NotificationCreatedEvent(
			UUID.randomUUID(),
			NotificationType.TICKET_STATUS_CHANGED,
			"title",
			"content",
			NotificationTargetType.TICKET,
			UUID.randomUUID()
		);
		EventEnvelope<NotificationCreatedEvent> event = EventEnvelope.of(
			"NOTIFICATION_CREATE_REQUESTED", companyId, payload
		);

		consumer.consume(objectMapper.valueToTree(event));

		verify(notificationService).persistNotificationEvent(event.eventId(), companyId, payload);
	}
}
