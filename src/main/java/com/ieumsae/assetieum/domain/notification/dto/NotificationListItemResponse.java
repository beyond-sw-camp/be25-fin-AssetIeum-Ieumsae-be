package com.ieumsae.assetieum.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ieumsae.assetieum.domain.notification.entity.Notification;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
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
	private final TicketType ticketType;
	@JsonProperty("isRead")
	private final boolean isRead;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime createdAt;

	public static NotificationListItemResponse from(Notification notification) {
		return from(notification, null);
	}

	public static NotificationListItemResponse from(Notification notification, TicketType ticketType) {
		return NotificationListItemResponse.builder()
			.notificationId(notification.getId())
			.notificationType(notification.getNotificationType())
			.title(notification.getTitle())
			.content(notification.getContent())
			.targetType(notification.getTargetType())
			.targetId(notification.getTargetId())
			.ticketType(ticketType)
			.isRead(notification.isRead())
			.createdAt(notification.getCreatedAt())
			.build();
	}
}
