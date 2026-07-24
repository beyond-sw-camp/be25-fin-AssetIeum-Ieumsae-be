package com.ieumsae.assetieum.global.kafka.smoke;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.kafka.smoke-test", name = "enabled", havingValue = "true")
public class KafkaSmokeTestConsumer {

	private final ObjectMapper objectMapper;

	@KafkaListener(
		topics = "${app.kafka.topics.smoke-test}",
		groupId = "${spring.kafka.consumer.group-id}-smoke"
	)
	public void consume(JsonNode message) {
		JavaType eventType = objectMapper.getTypeFactory()
			.constructParametricType(EventEnvelope.class, KafkaSmokeTestPayload.class);
		EventEnvelope<KafkaSmokeTestPayload> event = objectMapper.convertValue(message, eventType);

		log.info(
			"Kafka smoke-test event consumed. eventId={}, eventType={}, message={}",
			event.eventId(),
			event.eventType(),
			event.payload().message()
		);
	}
}
