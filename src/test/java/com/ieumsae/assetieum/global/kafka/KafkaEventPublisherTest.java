package com.ieumsae.assetieum.global.kafka;

import static org.mockito.Mockito.verify;

import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaEventPublisherTest {

	@Test
	void publishesEventWithTopicAndKey() {
		@SuppressWarnings("unchecked")
		KafkaTemplate<String, Object> kafkaTemplate = org.mockito.Mockito.mock(KafkaTemplate.class);
		KafkaEventPublisher publisher = new KafkaEventPublisher(kafkaTemplate);
		EventEnvelope<String> event = EventEnvelope.of("TEST_EVENT", UUID.randomUUID(), "payload");

		publisher.publish("assetieum.test.v1", event.eventId().toString(), event);

		verify(kafkaTemplate).send("assetieum.test.v1", event.eventId().toString(), event);
	}
}
