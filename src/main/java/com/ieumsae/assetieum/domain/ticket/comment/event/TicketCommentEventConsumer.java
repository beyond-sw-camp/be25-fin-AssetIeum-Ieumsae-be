package com.ieumsae.assetieum.domain.ticket.comment.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentEvent;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.kafka.ticket-comment", name = "enabled", havingValue = "true")
public class TicketCommentEventConsumer {

	private final ObjectMapper objectMapper;
	private final TicketCommentWebSocketPublisher webSocketPublisher;

	@KafkaListener(
		topics = "${app.kafka.topics.ticket-comment}",
		groupId = "${KAFKA_COMMENT_CONSUMER_GROUP_ID:assetieum-comment-${random.uuid}}",
		properties = "auto.offset.reset=latest"
	)
	public void consume(JsonNode message) {
		EventEnvelope<TicketCommentChangedEvent> envelope = objectMapper.convertValue(
			message,
			new TypeReference<>() {
			}
		);
		TicketCommentChangedEvent payload = envelope.payload();
		TicketCommentEvent<JsonNode> event = TicketCommentEvent.from(
			envelope.eventId(),
			payload.eventType(),
			payload.ticketId(),
			envelope.occurredAt(),
			payload.payload()
		);
		webSocketPublisher.publish(payload.ticketId(), event);
	}
}
