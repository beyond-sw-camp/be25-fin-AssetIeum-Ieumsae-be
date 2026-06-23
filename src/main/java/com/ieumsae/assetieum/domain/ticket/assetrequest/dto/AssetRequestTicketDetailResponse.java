package com.ieumsae.assetieum.domain.ticket.assetrequest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.assetrequest.type.AssetRequestTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketAssignmentTargetResponse;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetRequestTicketDetailResponse {

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
	private final AssetRequestTicketStatus detailStatus;
	private final UUID linkedPurchaseId;
	private final MemberSummary assetAssignee;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime assetProcessedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime assetAssignedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime assetRegisteredAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime processedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime completedAt;
	private final RequestedUsageType requestedUsageType;
	private final AssetType assetType;
	private final CategorySummary assetCategory;
	private final ItemSummary assetItem;
	private final int quantity;
	private final List<TicketAssignmentTargetResponse> assignmentTargets;
	private final MemberRole viewerRole;
	private final ViewOptions viewOptions;
	private final Actions actions;
	private final List<HistoryItem> histories;

	public static AssetRequestTicketDetailResponse from(
		Ticket ticket,
		AssetRequestTicket assetRequestTicket,
		MemberRole viewerRole,
		boolean requesterView,
		List<TicketAssignmentTargetResponse> assignmentTargets,
		Actions actions
	) {
		AssetType assetType = resolveAssetType(assetRequestTicket);
		return AssetRequestTicketDetailResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.title(ticket.getRequestReason())
			.requestReason(ticket.getRequestReason())
			.ticketType(ticket.getTicketType())
			.requester(MemberSummary.from(ticket.getRequester()))
			.department(DepartmentSummary.from(ticket))
			.departmentApprover(MemberSummary.from(ticket.getApprover()))
			.departmentProcessedAt(resolveDepartmentProcessedAt(ticket))
			.requestedAt(ticket.getCreatedAt())
			.currentStatus(ticket.getTicketStatus())
			.detailStatus(requesterView ? assetRequestTicket.getStatus() : null)
			.linkedPurchaseId(null)
			.assetAssignee(MemberSummary.from(ticket.getAssignee()))
			.assetProcessedAt(resolveAssetProcessedAt(ticket))
			.assetAssignedAt(resolveAssetAssignedAt(ticket, assetRequestTicket, viewerRole))
			.assetRegisteredAt(null)
			.processedAt(ticket.getUpdatedAt())
			.completedAt(ticket.getCompletedAt())
			.requestedUsageType(resolveRequestedUsageType(assetRequestTicket))
			.assetType(assetType)
			.assetCategory(CategorySummary.from(assetRequestTicket))
			.assetItem(ItemSummary.from(assetRequestTicket))
			.quantity(assetRequestTicket.getQuantity())
			.assignmentTargets(assignmentTargets)
			.viewerRole(viewerRole)
			.viewOptions(ViewOptions.from(requesterView))
			.actions(actions)
			.histories(createHistories(ticket))
			.build();
	}

	private static AssetType resolveAssetType(AssetRequestTicket ticket) {
		if (ticket.getTangibleAssetItem() != null) {
			return AssetType.TANGIBLE;
		}
		return AssetType.INTANGIBLE;
	}

	private static RequestedUsageType resolveRequestedUsageType(AssetRequestTicket ticket) {
		if (resolveAssetType(ticket) == AssetType.INTANGIBLE) {
			return null;
		}
		return ticket.getRequestedUsageType();
	}

	private static LocalDateTime resolveDepartmentProcessedAt(Ticket ticket) {
		if (ticket.getDepartmentApprovedAt() != null) {
			return ticket.getDepartmentApprovedAt();
		}
		return ticket.getDepartmentRejectedAt();
	}

	private static LocalDateTime resolveAssetProcessedAt(Ticket ticket) {
		if (ticket.getPurchaseApprovedAt() != null) {
			return ticket.getPurchaseApprovedAt();
		}
		return ticket.getPurchaseRejectedAt();
	}

	private static LocalDateTime resolveAssetAssignedAt(
		Ticket ticket,
		AssetRequestTicket assetRequestTicket,
		MemberRole viewerRole
	) {
		if (!canViewAssetAssignedAt(viewerRole)) {
			return null;
		}
		if (assetRequestTicket.getStatus() != AssetRequestTicketStatus.ASSIGNED
			&& assetRequestTicket.getStatus() != AssetRequestTicketStatus.COMPLETED) {
			return null;
		}
		return ticket.getCompletedAt() != null ? ticket.getCompletedAt() : ticket.getUpdatedAt();
	}

	private static boolean canViewAssetAssignedAt(MemberRole viewerRole) {
		return viewerRole == MemberRole.ASSET_MANAGER
			|| viewerRole == MemberRole.ASSET_TEAM
			|| viewerRole == MemberRole.ADMIN;
	}

	private static List<HistoryItem> createHistories(Ticket ticket) {
		List<HistoryItem> histories = new ArrayList<>();
		histories.add(HistoryItem.of(TicketStatus.REQUESTED.name(), ticket.getCreatedAt()));
		addHistory(histories, TicketStatus.DEPARTMENT_APPROVED.name(), ticket.getDepartmentApprovedAt());
		addHistory(histories, TicketStatus.DEPARTMENT_REJECTED.name(), ticket.getDepartmentRejectedAt());
		addHistory(histories, TicketStatus.IN_PROGRESS.name(), ticket.getPurchaseApprovedAt());
		addHistory(histories, TicketStatus.ASSET_REJECTED.name(), ticket.getPurchaseRejectedAt());
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

		private static DepartmentSummary from(Ticket ticket) {
			return DepartmentSummary.builder()
				.departmentId(ticket.getDepartment().getId())
				.name(ticket.getDepartment().getName())
				.build();
		}
	}

	@Getter
	@Builder
	public static class CategorySummary {
		private final UUID categoryId;
		private final String name;

		private static CategorySummary from(AssetRequestTicket ticket) {
			TangibleAssetItem tangibleItem = ticket.getTangibleAssetItem();
			if (tangibleItem != null) {
				TangibleAssetCategory category = tangibleItem.getTangibleAssetCategory();
				return CategorySummary.builder()
					.categoryId(category.getId())
					.name(category.getName())
					.build();
			}

			IntangibleAssetItem intangibleItem = ticket.getIntangibleAssetItem();
			IntangibleAssetCategory category = intangibleItem.getIntangibleAssetCategory();
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

		private static ItemSummary from(AssetRequestTicket ticket) {
			TangibleAssetItem tangibleItem = ticket.getTangibleAssetItem();
			if (tangibleItem != null) {
				return ItemSummary.builder()
					.itemId(tangibleItem.getId())
					.name(tangibleItem.getProductName())
					.manufacturer(tangibleItem.getManufacturer())
					.build();
			}

			IntangibleAssetItem intangibleItem = ticket.getIntangibleAssetItem();
			return ItemSummary.builder()
				.itemId(intangibleItem.getId())
				.name(intangibleItem.getProductName())
				.manufacturer(intangibleItem.getProvider())
				.build();
		}
	}

	@Getter
	@Builder
	public static class ViewOptions {
		private final boolean showDetailStatus;
		private final boolean showLinkedPurchaseId;

		private static ViewOptions from(boolean requesterView) {
			return ViewOptions.builder()
				.showDetailStatus(requesterView)
				.showLinkedPurchaseId(!requesterView)
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
