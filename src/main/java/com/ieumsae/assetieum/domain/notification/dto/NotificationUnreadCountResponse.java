package com.ieumsae.assetieum.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationUnreadCountResponse {

	private final long unreadCount;

	public static NotificationUnreadCountResponse from(long unreadCount) {
		return NotificationUnreadCountResponse.builder()
			.unreadCount(unreadCount)
			.build();
	}
}
