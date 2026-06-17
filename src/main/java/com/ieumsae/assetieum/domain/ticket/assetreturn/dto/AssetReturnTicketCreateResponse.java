package com.ieumsae.assetieum.domain.ticket.assetreturn.dto;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.assetreturn.entity.AssetReturnTicket;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetReturnTicketCreateResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketType ticketType;
	private final TicketStatus ticketStatus;
	private final AssetReturnTicketStatus assetReturnStatus;
	private final AssetReturnTargetType assetType;
	private final UUID assetId;
	private final String assetCode;
	private final UUID itemId;
	private final UUID categoryId;
	private final String categoryName;
	private final String productName;
	private final String requestReason;

	public static AssetReturnTicketCreateResponse from(Ticket ticket, AssetReturnTicket assetReturnTicket) {
		if (assetReturnTicket.getTangibleAsset() != null) {
			return fromTangible(ticket, assetReturnTicket);
		}

		return fromIntangible(ticket, assetReturnTicket);
	}

	private static AssetReturnTicketCreateResponse fromTangible(Ticket ticket, AssetReturnTicket assetReturnTicket) {
		TangibleAsset asset = assetReturnTicket.getTangibleAsset();
		TangibleAssetItem item = asset.getTangibleAssetItem();

		return AssetReturnTicketCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketType(ticket.getTicketType())
			.ticketStatus(ticket.getTicketStatus())
			.assetReturnStatus(assetReturnTicket.getStatus())
			.assetType(AssetReturnTargetType.TANGIBLE)
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.itemId(item.getId())
			.categoryId(item.getTangibleAssetCategory().getId())
			.categoryName(item.getTangibleAssetCategory().getName())
			.productName(item.getProductName())
			.requestReason(ticket.getRequestReason())
			.build();
	}

	private static AssetReturnTicketCreateResponse fromIntangible(Ticket ticket, AssetReturnTicket assetReturnTicket) {
		IntangibleAsset asset = assetReturnTicket.getIntangibleAsset();
		IntangibleAssetItem item = asset.getIntangibleAssetItem();

		return AssetReturnTicketCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketType(ticket.getTicketType())
			.ticketStatus(ticket.getTicketStatus())
			.assetReturnStatus(assetReturnTicket.getStatus())
			.assetType(AssetReturnTargetType.INTANGIBLE)
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.itemId(item.getId())
			.categoryId(item.getIntangibleAssetCategory().getId())
			.categoryName(item.getIntangibleAssetCategory().getName())
			.productName(item.getProductName())
			.requestReason(ticket.getRequestReason())
			.build();
	}
}
