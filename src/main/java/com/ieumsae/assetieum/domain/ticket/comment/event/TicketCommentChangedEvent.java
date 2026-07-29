package com.ieumsae.assetieum.domain.ticket.comment.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.ieumsae.assetieum.domain.ticket.comment.type.TicketCommentEventType;
import java.util.UUID;

public record TicketCommentChangedEvent(
	TicketCommentEventType eventType,
	UUID ticketId,
	JsonNode payload
) {
}
