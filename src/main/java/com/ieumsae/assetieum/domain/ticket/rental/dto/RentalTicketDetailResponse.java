package com.ieumsae.assetieum.domain.ticket.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.rental.entity.RentalTicket;
import com.ieumsae.assetieum.domain.ticket.rental.type.RentalTicketStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RentalTicketDetailResponse {

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
	private final RentalTicketStatus detailStatus;
	private final String departmentRejectionReason;
	private final String purchaseRejectionReason;
	private final MemberSummary assetAssignee;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime assetProcessedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime rentalStartDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime rentalDueDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime processedAt;
	private final RequestedUsageType requestedUsageType;
	private final AssetType assetType;
	private final CategorySummary assetCategory;
	private final ItemSummary assetItem;
	private final int quantity;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime requestedRentalStartDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime requestedReturnDueDate;
	private final MemberRole viewerRole;
	private final ViewOptions viewOptions;
	private final Actions actions;
	private final List<HistoryItem> histories;

	public static RentalTicketDetailResponse from(
		Ticket ticket,
		RentalTicket rentalTicket,
		MemberRole viewerRole,
		boolean requesterView,
		Actions actions
	) {
		return RentalTicketDetailResponse.builder()
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
			.detailStatus(requesterView ? rentalTicket.getStatus() : null)
			.departmentRejectionReason(ticket.getDepartmentRejectionReason())
			.purchaseRejectionReason(ticket.getPurchaseRejectionReason())
			.assetAssignee(MemberSummary.from(ticket.getAssignee()))
			.assetProcessedAt(resolveAssetProcessedAt(ticket))
			.rentalStartDate(rentalTicket.getRentalStartDate())
			.rentalDueDate(rentalTicket.getRentalDueDate())
			.processedAt(ticket.getUpdatedAt())
			.requestedUsageType(rentalTicket.getRequestedUsageType())
			.assetType(AssetType.TANGIBLE)
			.assetCategory(CategorySummary.from(rentalTicket))
			.assetItem(ItemSummary.from(rentalTicket))
			.quantity(1)
			.requestedRentalStartDate(rentalTicket.getRentalStartDate())
			.requestedReturnDueDate(rentalTicket.getRequestedDueDate())
			.viewerRole(viewerRole)
			.viewOptions(ViewOptions.from(requesterView))
			.actions(actions)
			.histories(createHistories(ticket))
			.build();
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

	private static List<HistoryItem> createHistories(Ticket ticket) {
		List<HistoryItem> histories = new ArrayList<>();
		histories.add(HistoryItem.of(TicketStatus.REQUESTED.name(), ticket.getCreatedAt()));
		addHistory(histories, TicketStatus.DEPARTMENT_APPROVED.name(), ticket.getDepartmentApprovedAt());
		addHistory(histories, TicketStatus.DEPARTMENT_REJECTED.name(), ticket.getDepartmentRejectedAt());
		addHistory(histories, TicketStatus.ASSET_APPROVED.name(), ticket.getPurchaseApprovedAt());
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

		private static CategorySummary from(RentalTicket ticket) {
			TangibleAssetCategory category = ticket.getTangibleAssetItem().getTangibleAssetCategory();
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

		private static ItemSummary from(RentalTicket ticket) {
			TangibleAssetItem item = ticket.getTangibleAssetItem();
			return ItemSummary.builder()
				.itemId(item.getId())
				.name(item.getProductName())
				.manufacturer(item.getManufacturer())
				.build();
		}
	}

	@Getter
	@Builder
	public static class ViewOptions {
		private final boolean showDetailStatus;

		private static ViewOptions from(boolean requesterView) {
			return ViewOptions.builder()
				.showDetailStatus(requesterView)
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
		private final boolean canUpdateReturnDueDate;
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
