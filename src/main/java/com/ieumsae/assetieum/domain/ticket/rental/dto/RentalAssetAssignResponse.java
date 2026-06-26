package com.ieumsae.assetieum.domain.ticket.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.rental.entity.RentalTicket;
import com.ieumsae.assetieum.domain.ticket.rental.type.RentalTicketStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RentalAssetAssignResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus ticketStatus;
	private final RentalTicketStatus rentalStatus;
	private final UUID requesterId;
	private final String requesterName;
	private final UUID assetId;
	private final String assetCode;
	private final String serialNumber;
	private final UUID itemId;
	private final String itemName;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime rentalStartDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime requestedDueDate;

	public static RentalAssetAssignResponse from(
		Ticket ticket,
		RentalTicket rentalTicket,
		TangibleAsset asset
	) {
		return RentalAssetAssignResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketStatus(ticket.getTicketStatus())
			.rentalStatus(rentalTicket.getStatus())
			.requesterId(ticket.getRequester().getId())
			.requesterName(ticket.getRequester().getName())
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.serialNumber(asset.getSerialNumber())
			.itemId(rentalTicket.getTangibleAssetItem().getId())
			.itemName(rentalTicket.getTangibleAssetItem().getProductName())
			.rentalStartDate(rentalTicket.getRentalStartDate())
			.requestedDueDate(rentalTicket.getRequestedDueDate())
			.build();
	}
}
