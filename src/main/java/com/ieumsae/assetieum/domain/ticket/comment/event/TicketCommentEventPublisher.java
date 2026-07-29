package com.ieumsae.assetieum.domain.ticket.comment.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.ticket.comment.type.TicketCommentEventType;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import com.ieumsae.assetieum.global.kafka.outbox.OutboxService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketCommentEventPublisher {

	private static final String EVENT_TYPE = "TICKET_COMMENT_CHANGED";

	private final OutboxService outboxService;
	private final ObjectMapper objectMapper;

	@Value("${app.kafka.topics.ticket-comment}")
	private String ticketCommentTopic;

	@Value("${app.kafka.ticket-comment.enabled:false}")
	private boolean enabled;

	public void publish(
		UUID companyId,
		UUID ticketId,
		TicketCommentEventType eventType,
		Object payload
	) {
		if (!enabled) {
			return;
		}
		EventEnvelope<TicketCommentChangedEvent> event = EventEnvelope.of(
			EVENT_TYPE,
			companyId,
			new TicketCommentChangedEvent(eventType, ticketId, objectMapper.valueToTree(payload))
		);
		outboxService.enqueue(ticketCommentTopic, ticketId.toString(), event);
	}

	public boolean isEnabled() {
		return enabled;
	}
}
