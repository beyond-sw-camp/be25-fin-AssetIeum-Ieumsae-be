package com.ieumsae.assetieum.domain.ticket.purchaserequest.dto;

import com.ieumsae.assetieum.domain.intangibleasset.item.type.LicenseType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.PurchaseRequestTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseRequestTicketCreateResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketType ticketType;
	private final TicketStatus ticketStatus;
	private final PurchaseRequestTicketStatus purchaseRequestStatus;
	private final RequestedUsageType requestedUsageType;
	private final RequestMethod requestMethod;
	private final AssetType assetType;
	private final Boolean isStandard;
	private final UUID assetItemId;
	private final UUID categoryId;
	private final String requestedItemDetail;
	private final String manufacturer;
	private final LicenseType licenseType;
	private final String purchaseUrl;
	private final int quantity;
	private final BigDecimal expectedPrice;

	public static PurchaseRequestTicketCreateResponse from(
		Ticket ticket,
		PurchaseRequestTicket purchaseRequestTicket,
		AssetType assetType,
		UUID categoryId
	) {
		return PurchaseRequestTicketCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketType(ticket.getTicketType())
			.ticketStatus(ticket.getTicketStatus())
			.purchaseRequestStatus(purchaseRequestTicket.getStatus())
			.requestedUsageType(purchaseRequestTicket.getRequestedUsageType())
			.requestMethod(purchaseRequestTicket.getRequestMethod())
			.assetType(assetType)
			.isStandard(purchaseRequestTicket.getIsStandard())
			.assetItemId(resolveAssetItemId(purchaseRequestTicket))
			.categoryId(resolveCategoryId(purchaseRequestTicket, categoryId))
			.requestedItemDetail(purchaseRequestTicket.getRequestedItemDetail())
			.manufacturer(purchaseRequestTicket.getManufacturer())
			.licenseType(purchaseRequestTicket.getLicenseType())
			.purchaseUrl(purchaseRequestTicket.getPurchaseUrl())
			.quantity(purchaseRequestTicket.getQuantity())
			.expectedPrice(purchaseRequestTicket.getExpectedPrice())
			.build();
	}

	private static UUID resolveAssetItemId(PurchaseRequestTicket ticket) {
		if (ticket.getTangibleAssetItem() != null) {
			return ticket.getTangibleAssetItem().getId();
		}
		if (ticket.getIntangibleAssetItem() != null) {
			return ticket.getIntangibleAssetItem().getId();
		}
		return null;
	}

	private static UUID resolveCategoryId(PurchaseRequestTicket ticket, UUID fallbackCategoryId) {
		if (ticket.getTangibleAssetCategory() != null) {
			return ticket.getTangibleAssetCategory().getId();
		}
		if (ticket.getIntangibleAssetCategory() != null) {
			return ticket.getIntangibleAssetCategory().getId();
		}
		return fallbackCategoryId;
	}
}
