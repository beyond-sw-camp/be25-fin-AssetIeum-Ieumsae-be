package com.ieumsae.assetieum.domain.ticket.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
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
public class RentalExtensionTicketCreateResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketType ticketType;
	private final TicketStatus ticketStatus;
	private final RentalTicketStatus rentalStatus;
	private final UUID assignmentId;
	private final UUID assetId;
	private final String assetCode;
	private final UUID tangibleAssetItemId;
	private final UUID categoryId;
	private final String categoryName;
	private final String productName;
	private final String serialNumber;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime currentReturnDueDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime requestedDueDate;

	public static RentalExtensionTicketCreateResponse from(
		Ticket ticket,
		RentalTicket rentalTicket,
		TangibleAssetAssignment assignment
	) {
		TangibleAsset asset = assignment.getTangibleAsset();
		TangibleAssetItem item = asset.getTangibleAssetItem();

		return RentalExtensionTicketCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketType(ticket.getTicketType())
			.ticketStatus(ticket.getTicketStatus())
			.rentalStatus(rentalTicket.getStatus())
			.assignmentId(assignment.getId())
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.tangibleAssetItemId(item.getId())
			.categoryId(item.getTangibleAssetCategory().getId())
			.categoryName(item.getTangibleAssetCategory().getName())
			.productName(item.getProductName())
			.serialNumber(asset.getSerialNumber())
			.currentReturnDueDate(asset.getReturnDueDate())
			.requestedDueDate(rentalTicket.getRequestedDueDate())
			.build();
	}
}
