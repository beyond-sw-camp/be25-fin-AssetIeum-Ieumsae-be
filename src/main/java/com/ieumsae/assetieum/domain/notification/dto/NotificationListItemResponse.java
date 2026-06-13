package com.ieumsae.assetieum.domain.notification.dto;

import com.ieumsae.assetieum.domain.notification.entity.Notification;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationListItemResponse {

	private final Long notificationId;
	private final NotificationType notificationType;
	private final String title;
	private final String content;
	private final NotificationTargetType targetType;
	private final UUID targetId;
	private final boolean read;
	private final LocalDateTime createdAt;

	public static NotificationListItemResponse from(Notification notification) {
		return NotificationListItemResponse.builder()
			.notificationId(notification.getId())
			.notificationType(notification.getNotificationType())
			.title(notification.getTitle())
			.content(notification.getContent())
			.targetType(notification.getTargetType())
			.targetId(notification.getTargetId())
			.read(notification.isRead())
			.createdAt(notification.getCreatedAt())
			.build();
	}
}
