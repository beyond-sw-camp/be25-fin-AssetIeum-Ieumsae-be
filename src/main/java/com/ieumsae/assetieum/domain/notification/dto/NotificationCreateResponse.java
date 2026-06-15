package com.ieumsae.assetieum.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationCreateResponse {

	private final int createdCount;

	public static NotificationCreateResponse from(int createdCount) {
		return NotificationCreateResponse.builder()
			.createdCount(createdCount)
			.build();
	}
}
