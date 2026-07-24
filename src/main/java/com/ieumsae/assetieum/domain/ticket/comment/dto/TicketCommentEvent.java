package com.ieumsae.assetieum.domain.ticket.comment.dto;

import com.ieumsae.assetieum.domain.ticket.comment.type.TicketCommentEventType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public record TicketCommentEvent<T>(
	UUID eventId,
	TicketCommentEventType eventType,
	UUID ticketId,
	LocalDateTime occurredAt,
	T payload
) {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

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

	public static <T> TicketCommentEvent<T> from(
		UUID eventId,
		TicketCommentEventType eventType,
		UUID ticketId,
		Instant occurredAt,
		T payload
	) {
		return new TicketCommentEvent<>(
			eventId,
			eventType,
			ticketId,
			LocalDateTime.ofInstant(occurredAt, SEOUL_ZONE),
			payload
		);
	}
}
