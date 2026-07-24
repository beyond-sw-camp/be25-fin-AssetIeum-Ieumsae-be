package com.ieumsae.assetieum.domain.ticket.comment.controller;

import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentCreateRequest;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentDeleteResponse;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentEvent;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentResponse;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentUpdateRequest;
import com.ieumsae.assetieum.domain.ticket.comment.event.TicketCommentEventPublisher;
import com.ieumsae.assetieum.domain.ticket.comment.event.TicketCommentWebSocketPublisher;
import com.ieumsae.assetieum.domain.ticket.comment.service.TicketCommentService;
import com.ieumsae.assetieum.domain.ticket.comment.type.TicketCommentEventType;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TicketCommentMessageController {

	private final TicketCommentService ticketCommentService;
	private final TicketCommentEventPublisher ticketCommentEventPublisher;
	private final TicketCommentWebSocketPublisher webSocketPublisher;

	@MessageMapping("/tickets/{ticketId}/comments/create")
	public void createComment(
		@DestinationVariable UUID ticketId,
		@Payload @Valid TicketCommentCreateRequest request,
		Principal principal
	) {
		AuthenticatedMember member = getAuthenticatedMember(principal);

		TicketCommentResponse response = ticketCommentService.createComment(
			member,
			ticketId,
			request
		);

		sendEvent(ticketId, TicketCommentEventType.CREATED, response);
	}

	@MessageMapping("/tickets/{ticketId}/comments/{commentId}/update")
	public void updateComment(
		@DestinationVariable UUID ticketId,
		@DestinationVariable Long commentId,
		@Payload @Valid TicketCommentUpdateRequest request,
		Principal principal
	) {
		AuthenticatedMember member = getAuthenticatedMember(principal);

		TicketCommentResponse response = ticketCommentService.updateComment(
			member,
			ticketId,
			commentId,
			request
		);

		sendEvent(ticketId, TicketCommentEventType.UPDATED, response);
	}

	@MessageMapping("/tickets/{ticketId}/comments/{commentId}/delete")
	public void deleteComment(
		@DestinationVariable UUID ticketId,
		@DestinationVariable Long commentId,
		Principal principal
	) {
		AuthenticatedMember member = getAuthenticatedMember(principal);

		TicketCommentDeleteResponse response = ticketCommentService.deleteComment(
			member,
			ticketId,
			commentId
		);

		sendEvent(ticketId, TicketCommentEventType.DELETED, response);
	}

	private <T> void sendEvent(
		UUID ticketId,
		TicketCommentEventType eventType,
		T payload
	) {
		if (ticketCommentEventPublisher.isEnabled()) {
			return;
		}
		TicketCommentEvent<T> event = TicketCommentEvent.of(
			eventType,
			ticketId,
			payload
		);

		webSocketPublisher.publish(ticketId, event);
	}

	private AuthenticatedMember getAuthenticatedMember(Principal principal) {
		Authentication authentication = (Authentication) principal;
		return (AuthenticatedMember) authentication.getPrincipal();
	}
}
