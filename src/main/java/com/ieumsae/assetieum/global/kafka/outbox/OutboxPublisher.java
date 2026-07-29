package com.ieumsae.assetieum.global.kafka.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.global.common.util.KstDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression(
	"${app.kafka.log.enabled:false} or "
		+ "${app.kafka.notification.enabled:false} or "
		+ "${app.kafka.ticket-comment.enabled:false}"
)
public class OutboxPublisher {

	private static final int BATCH_SIZE = 500;

	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Scheduled(fixedDelayString = "${app.kafka.outbox.publish-interval-ms:200}")
	@SchedulerLock(name = "kafkaOutboxPublisher", lockAtMostFor = "PT30S")
	public void publishPending() {
		List<OutboxEvent> events = outboxEventRepository.findPublishable(
			KstDateTime.now(),
			PageRequest.of(0, BATCH_SIZE)
		);
		List<PendingPublish> pendingPublishes = events.stream()
			.map(this::send)
			.toList();

		for (PendingPublish pendingPublish : pendingPublishes) {
			complete(pendingPublish);
		}
		if (!events.isEmpty()) {
			outboxEventRepository.saveAll(events);
		}
	}

	private PendingPublish send(OutboxEvent event) {
		try {
			JsonNode payload = objectMapper.readTree(event.getPayload());
			return new PendingPublish(
				event,
				kafkaTemplate.send(event.getTopic(), event.getEventKey(), payload)
			);
		} catch (JsonProcessingException exception) {
			event.markFailed(exception.getMessage());
			log.error("Invalid outbox payload. eventId={}", event.getEventId(), exception);
			return new PendingPublish(event, CompletableFuture.completedFuture(null));
		}
	}

	private void complete(PendingPublish pendingPublish) {
		OutboxEvent event = pendingPublish.event();
		if (event.getStatus() != OutboxStatus.PENDING) {
			return;
		}
		try {
			pendingPublish.future().get(10, TimeUnit.SECONDS);
			event.markPublished();
		} catch (Exception exception) {
			event.markFailed(exception.getMessage());
			log.warn("Outbox publish failed; it will be retried. eventId={}, retryCount={}",
				event.getEventId(), event.getRetryCount(), exception);
		}
	}

	private record PendingPublish(
		OutboxEvent event,
		CompletableFuture<?> future
	) {
	}
}
