package com.ieumsae.assetieum.domain.ticket.purchasereturn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.entity.PurchaseReturnTicket;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.type.PurchaseReturnTicketStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseReturnCollectResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus ticketStatus;
	private final PurchaseReturnTicketStatus detailStatus;
	private final boolean collected;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime collectedAt;
	private final String assetStatus;

	public static PurchaseReturnCollectResponse from(Ticket ticket, PurchaseReturnTicket purchaseReturnTicket) {
		return PurchaseReturnCollectResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketStatus(ticket.getTicketStatus())
			.detailStatus(purchaseReturnTicket.getStatus())
			.collected(purchaseReturnTicket.getCollectedAt() != null)
			.collectedAt(purchaseReturnTicket.getCollectedAt())
			.assetStatus(resolveAssetStatus(purchaseReturnTicket))
			.build();
	}

	private static String resolveAssetStatus(PurchaseReturnTicket purchaseReturnTicket) {
		if (purchaseReturnTicket.getTangibleAsset() != null) {
			return purchaseReturnTicket.getTangibleAsset().getTangibleAssetStatus().name();
		}
		return purchaseReturnTicket.getIntangibleAsset().getIntangibleAssetStatus().name();
	}
}
