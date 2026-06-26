package com.ieumsae.assetieum.domain.ticket.maintenance.dto;

import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.maintenance.entity.MaintenanceTicket;
import com.ieumsae.assetieum.domain.ticket.maintenance.type.MaintenanceTicketStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MaintenanceTicketCreateResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketType ticketType;
	private final TicketStatus ticketStatus;
	private final MaintenanceTicketStatus maintenanceStatus;
	private final UUID assignmentId;
	private final UUID assetId;
	private final String assetCode;
	private final UUID tangibleAssetItemId;
	private final UUID categoryId;
	private final String categoryName;
	private final String productName;
	private final String serialNumber;
	private final String requestDetail;

	public static MaintenanceTicketCreateResponse from(
		Ticket ticket,
		MaintenanceTicket maintenanceTicket,
		TangibleAssetAssignment assignment
	) {
		TangibleAsset asset = assignment.getTangibleAsset();
		TangibleAssetItem item = asset.getTangibleAssetItem();

		return MaintenanceTicketCreateResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketType(ticket.getTicketType())
			.ticketStatus(ticket.getTicketStatus())
			.maintenanceStatus(maintenanceTicket.getStatus())
			.assignmentId(assignment.getId())
			.assetId(asset.getId())
			.assetCode(asset.getAssetCode())
			.tangibleAssetItemId(item.getId())
			.categoryId(item.getTangibleAssetCategory().getId())
			.categoryName(item.getTangibleAssetCategory().getName())
			.productName(item.getProductName())
			.serialNumber(asset.getSerialNumber())
			.requestDetail(ticket.getRequestReason())
			.build();
	}
}
