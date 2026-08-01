package com.ieumsae.assetieum.domain.ticket.comment.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.ticket.comment.type.TicketCommentEventType;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import com.ieumsae.assetieum.global.kafka.outbox.OutboxService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class TicketCommentEventPublisherTest {

	private final OutboxService outboxService = mock(OutboxService.class);
	private final TicketCommentWebSocketPublisher webSocketPublisher =
		mock(TicketCommentWebSocketPublisher.class);
	private final TicketCommentEventPublisher publisher = new TicketCommentEventPublisher(
		outboxService, new ObjectMapper(), webSocketPublisher
	);

	@Test
	void enqueuesCommentEventUsingTicketIdAsKafkaKey() {
		ReflectionTestUtils.setField(publisher, "enabled", true);
		ReflectionTestUtils.setField(publisher, "ticketCommentTopic", "ticket-comments");
		UUID companyId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		ArgumentCaptor<EventEnvelope<?>> eventCaptor = ArgumentCaptor.forClass(EventEnvelope.class);

		publisher.publish(companyId, ticketId, TicketCommentEventType.CREATED, "comment");

		verify(outboxService).enqueue(
			org.mockito.ArgumentMatchers.eq("ticket-comments"),
			org.mockito.ArgumentMatchers.eq(ticketId.toString()),
			eventCaptor.capture()
		);
		EventEnvelope<?> envelope = eventCaptor.getValue();
		assertThat(envelope.companyId()).isEqualTo(companyId);
		assertThat(envelope.eventType()).isEqualTo("TICKET_COMMENT_CHANGED");
		assertThat(((TicketCommentChangedEvent) envelope.payload()).ticketId()).isEqualTo(ticketId);
	}

	@Test
	void publishesDirectlyToWebSocketWhenFeatureIsDisabled() {
		ReflectionTestUtils.setField(publisher, "enabled", false);
		UUID ticketId = UUID.randomUUID();

		publisher.publish(
			UUID.randomUUID(), ticketId, TicketCommentEventType.CREATED, "comment"
		);

		verifyNoInteractions(outboxService);
		verify(webSocketPublisher).publish(
			org.mockito.ArgumentMatchers.eq(ticketId),
			org.mockito.ArgumentMatchers.argThat(event ->
				event.eventType() == TicketCommentEventType.CREATED
					&& event.ticketId().equals(ticketId)
			)
		);
	}
}
