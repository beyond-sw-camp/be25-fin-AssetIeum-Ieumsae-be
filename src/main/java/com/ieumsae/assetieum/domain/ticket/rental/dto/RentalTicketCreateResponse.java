package com.ieumsae.assetieum.domain.ticket.rental.dto;

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
	private final TicketStatus status;
	private final RentalTicketStatus rentalStatus;
	private final RequestedUsageType requestedUsageType;
	private final UUID tangibleAssetItemId;
	private final UUID categoryId;
	private final String categoryName;
	private final String productName;
	private final LocalDateTime rentalStartDate;
	private final LocalDateTime requestedDueDate;
	private final LocalDateTime rentalDueDate;

	public static RentalTicketCreateResponse from(Ticket ticket, RentalTicket rentalTicket) {
		TangibleAssetItem item = rentalTicket.getTangibleAssetItem();

		return RentalTicketCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketType(ticket.getTicketType())
			.status(ticket.getTicketStatus())
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
