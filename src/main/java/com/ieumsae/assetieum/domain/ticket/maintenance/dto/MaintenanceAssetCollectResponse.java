package com.ieumsae.assetieum.domain.ticket.maintenance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.maintenance.entity.MaintenanceTicket;
import com.ieumsae.assetieum.domain.ticket.maintenance.type.MaintenanceTicketStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MaintenanceAssetCollectResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus currentStatus;
	private final MaintenanceTicketStatus detailStatus;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime collectedAt;
	private final boolean canCollectAsset;

	public static MaintenanceAssetCollectResponse from(Ticket ticket, MaintenanceTicket maintenanceTicket) {
		return MaintenanceAssetCollectResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.currentStatus(ticket.getTicketStatus())
			.detailStatus(maintenanceTicket.getStatus())
			.collectedAt(maintenanceTicket.getCollectedAt())
			.canCollectAsset(false)
			.build();
	}
}
