package com.ieumsae.assetieum.domain.notification.event;

import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import java.util.UUID;

public record NotificationCreatedEvent(
	UUID receiverId,
	NotificationType notificationType,
	String title,
	String content,
	NotificationTargetType targetType,
	UUID targetId
) {
}
