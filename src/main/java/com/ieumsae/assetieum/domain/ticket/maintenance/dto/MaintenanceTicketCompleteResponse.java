package com.ieumsae.assetieum.domain.ticket.maintenance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.maintenance.entity.MaintenanceTicket;
import com.ieumsae.assetieum.domain.ticket.maintenance.type.MaintenanceTicketStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MaintenanceTicketCompleteResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus currentStatus;
	private final MaintenanceTicketStatus detailStatus;
	private final String maintenanceResult;
	private final BigDecimal maintenanceCost;
	private final boolean budgetExecuted;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime maintenanceCompletedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime completedAt;

	public static MaintenanceTicketCompleteResponse from(
		Ticket ticket,
		MaintenanceTicket maintenanceTicket,
		boolean budgetExecuted
	) {
		return MaintenanceTicketCompleteResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.currentStatus(ticket.getTicketStatus())
			.detailStatus(maintenanceTicket.getStatus())
			.maintenanceResult(maintenanceTicket.getMaintenanceResult())
			.maintenanceCost(maintenanceTicket.getMaintenanceCost())
			.budgetExecuted(budgetExecuted)
			.maintenanceCompletedAt(maintenanceTicket.getMaintenanceCompletedAt())
			.completedAt(ticket.getCompletedAt())
			.build();
	}
}
