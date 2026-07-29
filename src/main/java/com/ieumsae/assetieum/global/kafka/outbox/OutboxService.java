package com.ieumsae.assetieum.global.kafka.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {

	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	@Transactional
	public void enqueue(String topic, String key, EventEnvelope<?> event) {
		try {
			outboxEventRepository.save(OutboxEvent.builder()
				.eventId(event.eventId())
				.topic(topic)
				.eventKey(key)
				.eventType(event.eventType())
				.payload(objectMapper.writeValueAsString(event))
				.build());
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize outbox event: " + event.eventId(), exception);
		}
	}
}
