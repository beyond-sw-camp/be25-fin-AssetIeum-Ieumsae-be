package com.ieumsae.assetieum.domain.ticket.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
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
	private final UUID requesterId;
	private final String requesterName;
	private final UUID departmentId;
	private final String departmentName;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime requestedAt;
	private final TicketStatus ticketStatus;

	public TicketListItemResponse(
		UUID ticketId,
		String ticketNo,
		TicketType ticketType,
		RequestMethod requestMethod,
		String requestedItemName,
		UUID requesterId,
		String requesterName,
		UUID departmentId,
		String departmentName,
		LocalDateTime requestedAt,
		TicketStatus ticketStatus
	) {
		this.ticketId = ticketId;
		this.ticketNo = ticketNo;
		this.ticketType = ticketType;
		this.requestMethod = requestMethod;
		this.requestedItemName = requestedItemName;
		this.requesterId = requesterId;
		this.requesterName = requesterName;
		this.departmentId = departmentId;
		this.departmentName = departmentName;
		this.requestedAt = requestedAt;
		this.ticketStatus = ticketStatus;
	}
}
