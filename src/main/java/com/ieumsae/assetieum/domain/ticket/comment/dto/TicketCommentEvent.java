package com.ieumsae.assetieum.domain.ticket.comment.dto;

import com.ieumsae.assetieum.domain.ticket.comment.type.TicketCommentEventType;
import java.time.LocalDateTime;
import java.util.UUID;

public record TicketCommentEvent<T>(
	UUID eventId,
	TicketCommentEventType eventType,
	UUID ticketId,
	LocalDateTime occurredAt,
	T payload
) {

	public static <T> TicketCommentEvent<T> of(
		TicketCommentEventType eventType,
		UUID ticketId,
		T payload
	) {
		return new TicketCommentEvent<>(
			UUID.randomUUID(),
			eventType,
			ticketId,
			LocalDateTime.now(),
			payload
		);
	}
}
