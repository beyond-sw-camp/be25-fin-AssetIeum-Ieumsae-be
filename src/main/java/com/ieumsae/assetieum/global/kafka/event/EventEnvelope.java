package com.ieumsae.assetieum.global.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
	UUID eventId,
	String eventType,
	Instant occurredAt,
	UUID companyId,
	T payload
) {

	public static <T> EventEnvelope<T> of(String eventType, UUID companyId, T payload) {
		return new EventEnvelope<>(
			UUID.randomUUID(),
			eventType,
			Instant.now(),
			companyId,
			payload
		);
	}
}
