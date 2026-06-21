package com.ieumsae.assetieum.domain.ticket.purchaserequest.dto;

import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.DirectPurchaseResult;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.ConfirmationStatus;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.PurchaseRequestTicketStatus;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectPurchaseAssetAssignResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus ticketStatus;
	private final PurchaseRequestTicketStatus purchaseRequestStatus;
	private final UUID requesterId;
	private final String requesterName;
	private final AssetType assetType;
	private final UUID itemId;
	private final String itemName;
	private final UUID assetId;
	private final String assetCode;
	private final UUID assignmentId;
	private final BigDecimal actualPrice;
	private final ConfirmationStatus confirmationStatus;

	public static DirectPurchaseAssetAssignResponse from(
		Ticket ticket,
		PurchaseRequestTicket purchaseRequestTicket,
		DirectPurchaseResult result,
		AssetType assetType,
		UUID itemId,
		String itemName,
		UUID assetId,
		String assetCode,
		UUID assignmentId
	) {
		return DirectPurchaseAssetAssignResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketStatus(ticket.getTicketStatus())
			.purchaseRequestStatus(purchaseRequestTicket.getStatus())
			.requesterId(ticket.getRequester().getId())
			.requesterName(ticket.getRequester().getName())
			.assetType(assetType)
			.itemId(itemId)
			.itemName(itemName)
			.assetId(assetId)
			.assetCode(assetCode)
			.assignmentId(assignmentId)
			.actualPrice(result.getActualPrice())
			.confirmationStatus(result.getConfirmationStatus())
			.build();
	}
}
