package com.ieumsae.assetieum.global.kafka.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

class OutboxPublisherTest {

	private final OutboxEventRepository repository = mock(OutboxEventRepository.class);
	@SuppressWarnings("unchecked")
	private final KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
	private final OutboxPublisher publisher = new OutboxPublisher(
		repository, new ObjectMapper(), kafkaTemplate
	);

	@Test
	void marksEventPublishedOnlyAfterKafkaAcknowledgesIt() {
		OutboxEvent event = pendingEvent();
		when(repository.findPublishable(any(), any(Pageable.class))).thenReturn(List.of(event));
		when(kafkaTemplate.send(eq(event.getTopic()), eq(event.getEventKey()), any()))
			.thenReturn(CompletableFuture.completedFuture(null));

		publisher.publishPending();

		verify(kafkaTemplate).send(eq(event.getTopic()), eq(event.getEventKey()), any());
		assertThat(event.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
		assertThat(event.getPublishedAt()).isNotNull();
	}

	@Test
	void schedulesRetryWhenKafkaPublishFails() {
		OutboxEvent event = pendingEvent();
		when(repository.findPublishable(any(), any(Pageable.class))).thenReturn(List.of(event));
		when(kafkaTemplate.send(eq(event.getTopic()), eq(event.getEventKey()), any()))
			.thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Kafka unavailable")));

		publisher.publishPending();

		assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
		assertThat(event.getRetryCount()).isEqualTo(1);
		assertThat(event.getNextRetryAt()).isNotNull();
		assertThat(event.getLastError()).contains("Kafka unavailable");
	}

	private OutboxEvent pendingEvent() {
		return OutboxEvent.builder()
			.eventId(UUID.randomUUID())
			.topic("assetieum.activity-log.v1")
			.eventKey("member-1")
			.eventType("ACTIVITY_LOG_CREATED")
			.payload("{\"eventId\":\"" + UUID.randomUUID() + "\"}")
			.status(OutboxStatus.PENDING)
			.build();
	}
}
