package com.ieumsae.assetieum.domain.ticket.assetreturn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.assetreturn.entity.AssetReturnTicket;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetReturnCompleteResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus ticketStatus;
	private final AssetReturnTicketStatus assetReturnStatus;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime returnProcessedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime completedAt;

	public static AssetReturnCompleteResponse from(Ticket ticket, AssetReturnTicket assetReturnTicket) {
		return AssetReturnCompleteResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketStatus(ticket.getTicketStatus())
			.assetReturnStatus(assetReturnTicket.getStatus())
			.returnProcessedAt(assetReturnTicket.getProcessedAt())
			.completedAt(ticket.getCompletedAt())
			.build();
	}
}
