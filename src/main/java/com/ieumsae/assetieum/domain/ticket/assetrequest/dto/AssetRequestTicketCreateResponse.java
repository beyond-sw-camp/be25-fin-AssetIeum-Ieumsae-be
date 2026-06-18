package com.ieumsae.assetieum.domain.ticket.assetrequest.dto;

import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.assetrequest.type.AssetRequestTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetRequestTicketCreateResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketType ticketType;
	private final TicketStatus ticketStatus;
	private final AssetRequestTicketStatus assetRequestStatus;
	private final RequestedUsageType requestedUsageType;
	private final AssetType assetType;
	private final UUID assetItemId;
	private final int quantity;

	public static AssetRequestTicketCreateResponse from(
		Ticket ticket,
		AssetRequestTicket assetRequestTicket,
		AssetType assetType,
		UUID assetItemId
	) {
		return AssetRequestTicketCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketType(ticket.getTicketType())
			.ticketStatus(ticket.getTicketStatus())
			.assetRequestStatus(assetRequestTicket.getStatus())
			.requestedUsageType(assetRequestTicket.getRequestedUsageType())
			.assetType(assetType)
			.assetItemId(assetItemId)
			.quantity(assetRequestTicket.getQuantity())
			.build();
	}
}
