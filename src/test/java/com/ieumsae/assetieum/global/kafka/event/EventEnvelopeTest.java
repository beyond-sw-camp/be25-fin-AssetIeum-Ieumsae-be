package com.ieumsae.assetieum.global.kafka.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventEnvelopeTest {

	@Test
	void createsEnvelopeWithEventMetadata() {
		UUID companyId = UUID.randomUUID();
		Instant beforeCreation = Instant.now();

		EventEnvelope<String> event = EventEnvelope.of("TEST_EVENT", companyId, "payload");

		assertThat(event.eventId()).isNotNull();
		assertThat(event.eventType()).isEqualTo("TEST_EVENT");
		assertThat(event.occurredAt()).isAfterOrEqualTo(beforeCreation);
		assertThat(event.companyId()).isEqualTo(companyId);
		assertThat(event.payload()).isEqualTo("payload");
	}
}
