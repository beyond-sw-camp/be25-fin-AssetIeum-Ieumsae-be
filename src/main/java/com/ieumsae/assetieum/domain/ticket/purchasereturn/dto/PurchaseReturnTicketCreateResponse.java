package com.ieumsae.assetieum.domain.ticket.purchasereturn.dto;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.entity.PurchaseReturnTicket;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.type.PurchaseReturnTicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.type.PurchaseReturnType;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseReturnTicketCreateResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketType ticketType;
	private final TicketStatus ticketStatus;
	private final PurchaseReturnTicketStatus purchaseReturnStatus;
	private final PurchaseReturnType purchaseReturnType;
	private final AssetReturnTargetType assetType;
	private final UUID assetId;
	private final String assetCode;
	private final UUID itemId;
	private final UUID categoryId;
	private final String categoryName;
	private final String productName;
	private final String manufacturer;
	private final String modelName;
	private final String provider;
	private final String serialNumber;
	private final String licenseCode;
	private final String requestReason;

	public static PurchaseReturnTicketCreateResponse from(
		Ticket ticket,
		PurchaseReturnTicket purchaseReturnTicket
	) {
		if (purchaseReturnTicket.getTangibleAsset() != null) {
			return fromTangible(ticket, purchaseReturnTicket);
		}

		return fromIntangible(ticket, purchaseReturnTicket);
	}

	private static PurchaseReturnTicketCreateResponse fromTangible(
		Ticket ticket,
		PurchaseReturnTicket purchaseReturnTicket
	) {
		TangibleAsset asset = purchaseReturnTicket.getTangibleAsset();
		TangibleAssetItem item = asset.getTangibleAssetItem();

		return baseBuilder(ticket, purchaseReturnTicket)
			.assetType(AssetReturnTargetType.TANGIBLE)
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.itemId(item.getId())
			.categoryId(item.getTangibleAssetCategory().getId())
			.categoryName(item.getTangibleAssetCategory().getName())
			.productName(item.getProductName())
			.manufacturer(item.getManufacturer())
			.modelName(item.getModelName())
			.serialNumber(asset.getSerialNumber())
			.build();
	}

	private static PurchaseReturnTicketCreateResponse fromIntangible(
		Ticket ticket,
		PurchaseReturnTicket purchaseReturnTicket
	) {
		IntangibleAsset asset = purchaseReturnTicket.getIntangibleAsset();
		IntangibleAssetItem item = asset.getIntangibleAssetItem();

		return baseBuilder(ticket, purchaseReturnTicket)
			.assetType(AssetReturnTargetType.INTANGIBLE)
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.itemId(item.getId())
			.categoryId(item.getIntangibleAssetCategory().getId())
			.categoryName(item.getIntangibleAssetCategory().getName())
			.productName(item.getProductName())
			.provider(item.getProvider())
			.licenseCode(asset.getLicenseCode())
			.build();
	}

	private static PurchaseReturnTicketCreateResponseBuilder baseBuilder(
		Ticket ticket,
		PurchaseReturnTicket purchaseReturnTicket
	) {
		return PurchaseReturnTicketCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketType(ticket.getTicketType())
			.ticketStatus(ticket.getTicketStatus())
			.purchaseReturnStatus(purchaseReturnTicket.getStatus())
			.purchaseReturnType(purchaseReturnTicket.getType())
			.requestReason(ticket.getRequestReason());
	}
}
