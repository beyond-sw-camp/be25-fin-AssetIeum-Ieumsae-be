package com.ieumsae.assetieum.domain.ticket.maintenance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.maintenance.entity.MaintenanceTicket;
import com.ieumsae.assetieum.domain.ticket.maintenance.type.MaintenanceTicketStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MaintenanceTicketDetailResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final String title;
	private final String requestReason;
	private final TicketType ticketType;
	private final MemberSummary requester;
	private final DepartmentSummary department;
	private final MemberSummary departmentApprover;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime departmentProcessedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime requestedAt;
	private final TicketStatus currentStatus;
	private final MaintenanceTicketStatus detailStatus;
	private final MemberSummary assetAssignee;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime collectedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime maintenanceCompletedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime completedAt;
	private final AssetType assetType;
	private final CategorySummary assetCategory;
	private final ItemSummary assetItem;
	private final UUID assetId;
	private final String requestDetail;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime processedAt;
	private final TicketStatus processingStatus;
	private final MemberRole viewerRole;
	private final ViewOptions viewOptions;
	private final Actions actions;
	private final List<HistoryItem> histories;

	public static MaintenanceTicketDetailResponse from(
		Ticket ticket,
		MaintenanceTicket maintenanceTicket,
		MemberRole viewerRole,
		boolean requesterView,
		Actions actions
	) {
		TangibleAsset asset = maintenanceTicket.getTangibleAsset();
		return MaintenanceTicketDetailResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.title(ticket.getRequestReason())
			.requestReason(ticket.getRequestReason())
			.ticketType(ticket.getTicketType())
			.requester(MemberSummary.from(ticket.getRequester()))
			.department(DepartmentSummary.from(ticket.getDepartment()))
			.departmentApprover(MemberSummary.from(ticket.getApprover()))
			.departmentProcessedAt(resolveDepartmentProcessedAt(ticket))
			.requestedAt(ticket.getCreatedAt())
			.currentStatus(ticket.getTicketStatus())
			.detailStatus(maintenanceTicket.getStatus())
			.assetAssignee(MemberSummary.from(ticket.getAssignee()))
			.collectedAt(maintenanceTicket.getCollectedAt())
			.maintenanceCompletedAt(maintenanceTicket.getMaintenanceCompletedAt())
			.completedAt(ticket.getCompletedAt())
			.assetType(AssetType.TANGIBLE)
			.assetCategory(CategorySummary.from(asset))
			.assetItem(ItemSummary.from(asset))
			.assetId(asset.getId())
			.requestDetail(ticket.getRequestReason())
			.processedAt(resolveProcessedAt(ticket, maintenanceTicket))
			.processingStatus(ticket.getTicketStatus())
			.viewerRole(viewerRole)
			.viewOptions(ViewOptions.from(requesterView))
			.actions(actions)
			.histories(createHistories(ticket, maintenanceTicket))
			.build();
	}

	private static LocalDateTime resolveDepartmentProcessedAt(Ticket ticket) {
		if (ticket.getDepartmentApprovedAt() != null) {
			return ticket.getDepartmentApprovedAt();
		}
		return ticket.getDepartmentRejectedAt();
	}

	private static LocalDateTime resolveProcessedAt(Ticket ticket, MaintenanceTicket maintenanceTicket) {
		if (ticket.getCompletedAt() != null) {
			return ticket.getCompletedAt();
		}
		if (ticket.getCancelledAt() != null) {
			return ticket.getCancelledAt();
		}
		if (maintenanceTicket.getMaintenanceCompletedAt() != null) {
			return maintenanceTicket.getMaintenanceCompletedAt();
		}
		if (maintenanceTicket.getCollectedAt() != null) {
			return maintenanceTicket.getCollectedAt();
		}
		if (ticket.getPurchaseApprovedAt() != null) {
			return ticket.getPurchaseApprovedAt();
		}
		if (ticket.getPurchaseRejectedAt() != null) {
			return ticket.getPurchaseRejectedAt();
		}
		return resolveDepartmentProcessedAt(ticket);
	}

	private static List<HistoryItem> createHistories(Ticket ticket, MaintenanceTicket maintenanceTicket) {
		List<HistoryItem> histories = new ArrayList<>();
		histories.add(HistoryItem.of(TicketStatus.REQUESTED.name(), ticket.getCreatedAt()));
		addHistory(histories, TicketStatus.DEPARTMENT_APPROVED.name(), ticket.getDepartmentApprovedAt());
		addHistory(histories, TicketStatus.DEPARTMENT_REJECTED.name(), ticket.getDepartmentRejectedAt());
		addHistory(histories, TicketStatus.ASSET_APPROVED.name(), ticket.getPurchaseApprovedAt());
		addHistory(histories, TicketStatus.ASSET_REJECTED.name(), ticket.getPurchaseRejectedAt());
		addHistory(histories, MaintenanceTicketStatus.COLLECTED.name(), maintenanceTicket.getCollectedAt());
		addHistory(histories, MaintenanceTicketStatus.COMPLETED.name(), maintenanceTicket.getMaintenanceCompletedAt());
		addHistory(histories, TicketStatus.COMPLETED.name(), ticket.getCompletedAt());
		addHistory(histories, TicketStatus.CANCELLED.name(), ticket.getCancelledAt());
		return histories;
	}

	private static void addHistory(List<HistoryItem> histories, String status, LocalDateTime processedAt) {
		if (processedAt != null) {
			histories.add(HistoryItem.of(status, processedAt));
		}
	}

	@Getter
	@Builder
	public static class MemberSummary {
		private final UUID memberId;
		private final String memberNo;
		private final String name;
		private final MemberRole role;

		private static MemberSummary from(Member member) {
			if (member == null) {
				return null;
			}
			return MemberSummary.builder()
				.memberId(member.getId())
				.memberNo(member.getMemberNo())
				.name(member.getName())
				.role(member.getRole())
				.build();
		}
	}

	@Getter
	@Builder
	public static class DepartmentSummary {
		private final UUID departmentId;
		private final String name;

		private static DepartmentSummary from(Department department) {
			return DepartmentSummary.builder()
				.departmentId(department.getId())
				.name(department.getName())
				.build();
		}
	}

	@Getter
	@Builder
	public static class CategorySummary {
		private final UUID categoryId;
		private final String name;

		private static CategorySummary from(TangibleAsset asset) {
			TangibleAssetCategory category = asset.getTangibleAssetItem().getTangibleAssetCategory();
			return CategorySummary.builder()
				.categoryId(category.getId())
				.name(category.getName())
				.build();
		}
	}

	@Getter
	@Builder
	public static class ItemSummary {
		private final UUID itemId;
		private final String name;
		private final String manufacturer;
		private final String modelName;

		private static ItemSummary from(TangibleAsset asset) {
			TangibleAssetItem item = asset.getTangibleAssetItem();
			return ItemSummary.builder()
				.itemId(item.getId())
				.name(item.getProductName())
				.manufacturer(item.getManufacturer())
				.modelName(item.getModelName())
				.build();
		}
	}

	@Getter
	@Builder
	public static class ViewOptions {
		private final boolean showDetailStatus;
		private final boolean showDepartmentApprovalActions;
		private final boolean showAssetActions;

		private static ViewOptions from(boolean requesterView) {
			return ViewOptions.builder()
				.showDetailStatus(requesterView)
				.showDepartmentApprovalActions(!requesterView)
				.showAssetActions(!requesterView)
				.build();
		}
	}

	@Getter
	@Builder
	public static class Actions {
		private final boolean canApproveDepartment;
		private final boolean canRejectDepartment;
		private final boolean canAssignAsset;
		private final boolean canApproveAsset;
		private final boolean canRejectAsset;
		private final boolean canChangeProcessingStatus;
		private final boolean canCollectAsset;
		private final boolean canCompleteMaintenance;
	}

	@Getter
	@Builder
	public static class HistoryItem {
		private final String status;
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private final LocalDateTime processedAt;

		private static HistoryItem of(String status, LocalDateTime processedAt) {
			return HistoryItem.builder()
				.status(status)
				.processedAt(processedAt)
				.build();
		}
	}
}
