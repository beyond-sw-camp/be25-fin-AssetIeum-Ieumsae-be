package com.ieumsae.assetieum.domain.ticket.common.dto;

import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketAssigneeResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus ticketStatus;
	private final UUID assigneeId;
	private final String assigneeName;

	public static TicketAssigneeResponse from(Ticket ticket) {
		return TicketAssigneeResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketStatus(ticket.getTicketStatus())
			.assigneeId(ticket.getAssignee().getId())
			.assigneeName(ticket.getAssignee().getName())
			.build();
	}
}
