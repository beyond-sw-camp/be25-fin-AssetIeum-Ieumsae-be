package com.ieumsae.assetieum.domain.ticket.dto;

import com.ieumsae.assetieum.domain.ticket.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.type.TicketStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StandardAssetRequestCreateResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus status;

	public static StandardAssetRequestCreateResponse from(Ticket ticket) {
		return StandardAssetRequestCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.status(ticket.getTicketStatus())
			.build();
	}
}
