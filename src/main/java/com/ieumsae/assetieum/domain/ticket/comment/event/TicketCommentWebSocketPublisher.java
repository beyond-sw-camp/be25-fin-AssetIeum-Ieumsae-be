package com.ieumsae.assetieum.domain.ticket.comment.event;

import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketCommentWebSocketPublisher {

	private static final String COMMENT_TOPIC = "/topic/tickets/%s/comments";

	private final SimpMessagingTemplate messagingTemplate;

	public void publish(UUID ticketId, TicketCommentEvent<?> event) {
		messagingTemplate.convertAndSend(COMMENT_TOPIC.formatted(ticketId), event);
	}
}
