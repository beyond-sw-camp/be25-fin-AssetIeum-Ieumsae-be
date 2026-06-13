package com.ieumsae.assetieum.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationReadResponse {

	private final Long notificationId;
	private final boolean read;

	public static NotificationReadResponse from(Long notificationId) {
		return NotificationReadResponse.builder()
			.notificationId(notificationId)
			.read(true)
			.build();
	}
}
