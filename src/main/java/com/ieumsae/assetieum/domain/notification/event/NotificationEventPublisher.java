package com.ieumsae.assetieum.domain.notification.event;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import com.ieumsae.assetieum.global.kafka.outbox.OutboxService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

	private static final String EVENT_TYPE = "NOTIFICATION_CREATE_REQUESTED";

	private final OutboxService outboxService;

	@Value("${app.kafka.topics.notification}")
	private String notificationTopic;

	public void publish(
		Member receiver,
		NotificationType notificationType,
		String title,
		String content,
		NotificationTargetType targetType,
		UUID targetId
	) {
		EventEnvelope<NotificationCreatedEvent> event = EventEnvelope.of(
			EVENT_TYPE,
			receiver.getCompany().getId(),
			new NotificationCreatedEvent(
				receiver.getId(), notificationType, title, content, targetType, targetId
			)
		);
		outboxService.enqueue(notificationTopic, receiver.getId().toString(), event);
	}
}
