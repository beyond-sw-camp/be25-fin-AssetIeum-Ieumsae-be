package com.ieumsae.assetieum.domain.notification.dto;

import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationSseMessage(
	UUID receiverId,
	Long notificationId,
	NotificationType notificationType,
	String title,
	String content,
	NotificationTargetType targetType,
	UUID targetId,
	boolean isRead,
	LocalDateTime createdAt
) {

	public static NotificationSseMessage from(UUID receiverId, NotificationListItemResponse response) {
		return new NotificationSseMessage(
			receiverId,
			response.getNotificationId(),
			response.getNotificationType(),
			response.getTitle(),
			response.getContent(),
			response.getTargetType(),
			response.getTargetId(),
			response.isRead(),
			response.getCreatedAt()
		);
	}

	public NotificationListItemResponse toResponse() {
		return NotificationListItemResponse.builder()
			.notificationId(notificationId)
			.notificationType(notificationType)
			.title(title)
			.content(content)
			.targetType(targetType)
			.targetId(targetId)
			.isRead(isRead)
			.createdAt(createdAt)
			.build();
	}
}
