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
public class AssetApprovalResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus ticketStatus;
	private final UUID assetAssigneeId;
	private final String assetAssigneeName;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime assetApprovedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime assetRejectedAt;
	private final String assetRejectionReason;

	public static AssetApprovalResponse from(Ticket ticket) {
		return AssetApprovalResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketStatus(ticket.getTicketStatus())
			.assetAssigneeId(ticket.getAssignee() == null ? null : ticket.getAssignee().getId())
			.assetAssigneeName(ticket.getAssignee() == null ? null : ticket.getAssignee().getName())
			.assetApprovedAt(ticket.getPurchaseApprovedAt())
			.assetRejectedAt(ticket.getPurchaseRejectedAt())
			.assetRejectionReason(ticket.getPurchaseRejectionReason())
			.build();
	}
}
