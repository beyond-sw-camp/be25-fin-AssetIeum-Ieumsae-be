package com.ieumsae.assetieum.domain.ticket.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketCancelResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus ticketStatus;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime cancelledAt;

	public static TicketCancelResponse from(Ticket ticket) {
		return TicketCancelResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketStatus(ticket.getTicketStatus())
			.cancelledAt(ticket.getCancelledAt())
			.build();
	}
}
