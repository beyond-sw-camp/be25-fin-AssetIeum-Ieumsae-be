package com.ieumsae.assetieum.domain.ticket.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.rental.entity.RentalTicket;
import com.ieumsae.assetieum.domain.ticket.rental.type.RentalTicketStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RentalTicketCreateResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketType ticketType;
	private final TicketStatus ticketStatus;
	private final RentalTicketStatus rentalStatus;
	private final RequestedUsageType requestedUsageType;
	private final UUID tangibleAssetItemId;
	private final UUID categoryId;
	private final String categoryName;
	private final String productName;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime rentalStartDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime requestedDueDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime rentalDueDate;

	public static RentalTicketCreateResponse from(Ticket ticket, RentalTicket rentalTicket) {
		TangibleAssetItem item = rentalTicket.getTangibleAssetItem();

		return RentalTicketCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketType(ticket.getTicketType())
			.ticketStatus(ticket.getTicketStatus())
			.rentalStatus(rentalTicket.getStatus())
			.requestedUsageType(rentalTicket.getRequestedUsageType())
			.tangibleAssetItemId(item.getId())
			.categoryId(item.getTangibleAssetCategory().getId())
			.categoryName(item.getTangibleAssetCategory().getName())
			.productName(item.getProductName())
			.rentalStartDate(rentalTicket.getRentalStartDate())
			.requestedDueDate(rentalTicket.getRequestedDueDate())
			.rentalDueDate(rentalTicket.getRentalDueDate())
			.build();
	}
}
