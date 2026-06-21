package com.ieumsae.assetieum.domain.ticket.purchasereturn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.entity.PurchaseReturnTicket;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.type.PurchaseReturnTicketStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseReturnCompleteResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus ticketStatus;
	private final PurchaseReturnTicketStatus detailStatus;
	private final BigDecimal refundAmount;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime returnProcessedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime completedAt;

	public static PurchaseReturnCompleteResponse from(Ticket ticket, PurchaseReturnTicket purchaseReturnTicket) {
		return PurchaseReturnCompleteResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketStatus(ticket.getTicketStatus())
			.detailStatus(purchaseReturnTicket.getStatus())
			.refundAmount(resolveRefundAmount(purchaseReturnTicket))
			.returnProcessedAt(purchaseReturnTicket.getShippedAt())
			.completedAt(ticket.getCompletedAt())
			.build();
	}

	private static BigDecimal resolveRefundAmount(PurchaseReturnTicket purchaseReturnTicket) {
		if (purchaseReturnTicket.getTangibleAsset() != null) {
			return purchaseReturnTicket.getTangibleAsset().getPurchasePrice();
		}
		return purchaseReturnTicket.getIntangibleAsset().getPurchasePrice();
	}
}
