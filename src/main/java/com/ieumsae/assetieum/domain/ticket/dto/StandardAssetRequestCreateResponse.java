package com.ieumsae.assetieum.domain.ticket.dto;

import com.ieumsae.assetieum.domain.ticket.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.type.TicketType;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StandardAssetRequestCreateResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketType ticketType;
	private final TicketStatus status;
	private final RequestedUsageType requestedUsageType;
	private final AssetType assetType;
	private final UUID assetItemId;
	private final int quantity;

	public static StandardAssetRequestCreateResponse from(
		Ticket ticket,
		RequestedUsageType requestedUsageType,
		AssetType assetType,
		UUID assetItemId,
		int quantity
	) {
		return StandardAssetRequestCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketType(ticket.getTicketType())
			.status(ticket.getTicketStatus())
			.requestedUsageType(requestedUsageType)
			.assetType(assetType)
			.assetItemId(assetItemId)
			.quantity(quantity)
			.build();
	}
}
