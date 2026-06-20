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
public class RentalExtensionDueDateUpdateResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus ticketStatus;
	private final RentalTicketStatus rentalStatus;
	private final UUID assetId;
	private final String assetCode;
	private final String serialNumber;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime previousReturnDueDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime returnDueDate;

	public static RentalExtensionDueDateUpdateResponse from(
		Ticket ticket,
		RentalTicket rentalTicket,
		TangibleAsset asset,
		LocalDateTime previousReturnDueDate
	) {
		return RentalExtensionDueDateUpdateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketStatus(ticket.getTicketStatus())
			.rentalStatus(rentalTicket.getStatus())
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.serialNumber(asset.getSerialNumber())
			.previousReturnDueDate(previousReturnDueDate)
			.returnDueDate(asset.getReturnDueDate())
			.build();
	}
}
