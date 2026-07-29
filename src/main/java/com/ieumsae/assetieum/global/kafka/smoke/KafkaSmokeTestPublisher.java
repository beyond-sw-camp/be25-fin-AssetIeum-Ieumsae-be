package com.ieumsae.assetieum.global.kafka.smoke;

import com.ieumsae.assetieum.global.kafka.KafkaEventPublisher;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.kafka.smoke-test", name = "enabled", havingValue = "true")
public class KafkaSmokeTestPublisher {

	private static final String EVENT_TYPE = "KAFKA_SMOKE_TEST";

	private final KafkaEventPublisher kafkaEventPublisher;

	@Value("${app.kafka.topics.smoke-test}")
	private String topic;

	@EventListener(ApplicationReadyEvent.class)
	public void publishAfterStartup() {
		EventEnvelope<KafkaSmokeTestPayload> event = EventEnvelope.of(
			EVENT_TYPE,
			null,
			new KafkaSmokeTestPayload("Kafka connection is working")
		);

		kafkaEventPublisher.publish(topic, event.eventId().toString(), event)
			.whenComplete((result, exception) -> {
				if (exception != null) {
					log.error("Kafka smoke-test event publish failed. eventId={}", event.eventId(), exception);
					return;
				}
				log.info(
					"Kafka smoke-test event published. eventId={}, topic={}, partition={}, offset={}",
					event.eventId(),
					result.getRecordMetadata().topic(),
					result.getRecordMetadata().partition(),
					result.getRecordMetadata().offset()
				);
			});
	}
}
