package com.ieumsae.assetieum.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationReadAllResponse {

	private final int updatedCount;

	public static NotificationReadAllResponse from(int updatedCount) {
		return NotificationReadAllResponse.builder()
			.updatedCount(updatedCount)
			.build();
	}
}
