package com.ieumsae.assetieum.domain.ticket.comment.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentEvent;
import com.ieumsae.assetieum.domain.ticket.comment.type.TicketCommentEventType;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TicketCommentEventConsumerTest {

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
	private final TicketCommentWebSocketPublisher webSocketPublisher = mock(TicketCommentWebSocketPublisher.class);
	private final TicketCommentEventConsumer consumer = new TicketCommentEventConsumer(
		objectMapper, webSocketPublisher
	);

	@Test
	void broadcastsKafkaEventToLocalWebSocketSubscribers() {
		UUID companyId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		TicketCommentChangedEvent payload = new TicketCommentChangedEvent(
			TicketCommentEventType.UPDATED,
			ticketId,
			objectMapper.createObjectNode().put("content", "updated")
		);
		EventEnvelope<TicketCommentChangedEvent> envelope = EventEnvelope.of(
			"TICKET_COMMENT_CHANGED", companyId, payload
		);
		ArgumentCaptor<TicketCommentEvent<?>> eventCaptor = ArgumentCaptor.forClass(TicketCommentEvent.class);

		consumer.consume(objectMapper.valueToTree(envelope));

		verify(webSocketPublisher).publish(
			org.mockito.ArgumentMatchers.eq(ticketId),
			eventCaptor.capture()
		);
		TicketCommentEvent<?> event = eventCaptor.getValue();
		assertThat(event.eventId()).isEqualTo(envelope.eventId());
		assertThat(event.eventType()).isEqualTo(TicketCommentEventType.UPDATED);
		assertThat(event.ticketId()).isEqualTo(ticketId);
	}
}
