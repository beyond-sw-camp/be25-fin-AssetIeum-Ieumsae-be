package com.ieumsae.assetieum.domain.ticket.purchaserequest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.BillingCycle;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.DirectPurchaseResult;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.ConfirmationStatus;
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
	private final BigDecimal expectedTotalPrice;   // 예상 합계금액 (단가 × 수량)
	private final BigDecimal priceDifference;      // 차액 (실제 - 예상합계)
	private final String proofFileUrl;             // 증빙 파일 URL
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime proofFileUploadedAt; // 증빙 파일 업로드 일시
	private final ConfirmationStatus confirmationStatus; // 확인 상태

	public static DirectPurchaseResultCreateResponse from(
		Ticket ticket,
		DirectPurchaseResult result,
		AssetType assetType
	) {
		PurchaseRequestTicket purchaseRequestTicket = result.getPurchaseRequestTicket();
		BigDecimal expectedTotal = purchaseRequestTicket.getExpectedPrice() != null
			? purchaseRequestTicket.getExpectedPrice()
				.multiply(BigDecimal.valueOf(purchaseRequestTicket.getQuantity()))
			: null;
		BigDecimal diff = (expectedTotal != null && result.getActualPrice() != null)
			? result.getActualPrice().subtract(expectedTotal)
			: null;

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
			.expectedTotalPrice(expectedTotal)
			.priceDifference(diff)
			.proofFileUrl(result.getProofFileUrl())
			.proofFileUploadedAt(result.getProofFileUploadedAt())
			.confirmationStatus(result.getConfirmationStatus())
			.build();
	}
}
