package com.ieumsae.assetieum.domain.ticket.common.dto;

import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class TicketListItemResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketType ticketType;
	private final RequestMethod requestMethod;
	private final String requestedItemName;
	private final LocalDateTime requestedAt;
	private final TicketStatus ticketStatus;

	public TicketListItemResponse(
		UUID ticketId,
		String ticketNo,
		TicketType ticketType,
		RequestMethod requestMethod,
		String requestedItemName,
		LocalDateTime requestedAt,
		TicketStatus ticketStatus
	) {
		this.ticketId = ticketId;
		this.ticketNo = ticketNo;
		this.ticketType = ticketType;
		this.requestMethod = requestMethod;
		this.requestedItemName = requestedItemName;
		this.requestedAt = requestedAt;
		this.ticketStatus = ticketStatus;
	}
}
