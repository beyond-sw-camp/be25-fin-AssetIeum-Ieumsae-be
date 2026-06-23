package com.ieumsae.assetieum.domain.ticket.assetrequest.dto;

import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.assetrequest.type.AssetRequestTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetRequestAssignResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus ticketStatus;
	private final AssetRequestTicketStatus assetRequestStatus;
	private final UUID requesterId;
	private final String requesterName;
	private final AssetType assetType;
	private final UUID itemId;
	private final String itemName;
	private final int assignedQuantity;
	private final List<AssignedAssetSummary> assignedAssets;

	public static AssetRequestAssignResponse from(
		Ticket ticket,
		AssetRequestTicket assetRequestTicket,
		AssetType assetType,
		UUID itemId,
		String itemName,
		List<AssignedAssetSummary> assignedAssets
	) {
		return AssetRequestAssignResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketStatus(ticket.getTicketStatus())
			.assetRequestStatus(assetRequestTicket.getStatus())
			.requesterId(ticket.getRequester().getId())
			.requesterName(ticket.getRequester().getName())
			.assetType(assetType)
			.itemId(itemId)
			.itemName(itemName)
			.assignedQuantity(assignedAssets.size())
			.assignedAssets(assignedAssets)
			.build();
	}

	@Getter
	@Builder
	public static class AssignedAssetSummary {

		private final UUID assetId;
		private final String assetCode;
		private final UUID assigneeId;
		private final String assigneeName;
		private final UUID departmentId;
		private final String departmentName;
	}
}
