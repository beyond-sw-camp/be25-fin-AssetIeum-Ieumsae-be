package com.ieumsae.assetieum.global.kafka;

import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public CompletableFuture<SendResult<String, Object>> publish(
		String topic,
		String key,
		EventEnvelope<?> event
	) {
		return kafkaTemplate.send(topic, key, event);
	}
}
