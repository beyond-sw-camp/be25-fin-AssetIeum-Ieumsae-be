package com.ieumsae.assetieum.domain.ticket.purchaserequest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.BillingCycle;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.DirectPurchaseResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectPurchaseResultCreateResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketType ticketType;
	private final TicketStatus ticketStatus;
	private final AssetType assetType;
	private final BigDecimal actualPrice;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime purchaseDate;
	private final String purchaseVendor;
	private final String serialNumber;
	private final String location;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime warrantyExpiredAt;
	private final String licenseCode;
	private final Integer seatCount;
	private final Boolean isAutoRenewal;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime startedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime expiredAt;
	private final BillingCycle billingCycle;

	public static DirectPurchaseResultCreateResponse from(
		Ticket ticket,
		DirectPurchaseResult result,
		AssetType assetType
	) {
		return DirectPurchaseResultCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketType(ticket.getTicketType())
			.ticketStatus(ticket.getTicketStatus())
			.assetType(assetType)
			.actualPrice(result.getActualPrice())
			.purchaseDate(result.getPurchaseDate())
			.purchaseVendor(result.getPurchaseVendor())
			.serialNumber(result.getSerialNumber())
			.location(result.getLocation())
			.warrantyExpiredAt(result.getWarrantyExpiredAt())
			.licenseCode(result.getLicenseCode())
			.seatCount(result.getSeatCount())
			.isAutoRenewal(result.getIsAutoRenewal())
			.startedAt(result.getStartedAt())
			.expiredAt(result.getExpiredAt())
			.billingCycle(result.getBillingCycle())
			.build();
	}
}
