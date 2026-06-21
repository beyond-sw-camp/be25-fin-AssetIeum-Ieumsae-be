package com.ieumsae.assetieum.domain.ticket.assetreturn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.assetreturn.entity.AssetReturnTicket;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetReturnTicketDetailResponse {

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
	private final AssetReturnTicketStatus detailStatus;
	private final MemberSummary assetAssignee;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime collectedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime returnProcessedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime completedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime processedAt;
	private final AssetReturnTargetType assetType;
	private final CategorySummary assetCategory;
	private final ItemSummary assetItem;
	private final UUID assetId;
	private final String assetCode;
	private final String assetStatus;
	private final int quantity;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime usedStartedAt;
	private final boolean collected;
	private final RequestDetail requestDetail;
	private final MemberRole viewerRole;
	private final ViewOptions viewOptions;
	private final Actions actions;
	private final List<HistoryItem> histories;

	public static AssetReturnTicketDetailResponse from(
		Ticket ticket,
		AssetReturnTicket assetReturnTicket,
		MemberRole viewerRole,
		boolean requesterView,
		Actions actions
	) {
		AssetReturnTargetType assetType = assetReturnTicket.getTangibleAsset() != null
			? AssetReturnTargetType.TANGIBLE
			: AssetReturnTargetType.INTANGIBLE;

		return AssetReturnTicketDetailResponse.builder()
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
			.detailStatus(assetReturnTicket.getStatus())
			.assetAssignee(MemberSummary.from(ticket.getAssignee()))
			.collectedAt(assetReturnTicket.getCollectedAt())
			.returnProcessedAt(assetReturnTicket.getProcessedAt())
			.completedAt(ticket.getCompletedAt())
			.processedAt(resolveProcessedAt(ticket, assetReturnTicket))
			.assetType(assetType)
			.assetCategory(CategorySummary.from(assetReturnTicket))
			.assetItem(ItemSummary.from(assetReturnTicket))
			.assetId(resolveAssetId(assetReturnTicket))
			.assetCode(resolveAssetCode(assetReturnTicket))
			.assetStatus(resolveAssetStatus(assetReturnTicket))
			.quantity(1)
			.usedStartedAt(resolveUsedStartedAt(assetReturnTicket))
			.collected(assetReturnTicket.getCollectedAt() != null)
			.requestDetail(RequestDetail.from(ticket, assetReturnTicket))
			.viewerRole(viewerRole)
			.viewOptions(ViewOptions.from(requesterView))
			.actions(actions)
			.histories(createHistories(ticket, assetReturnTicket))
			.build();
	}

	private static LocalDateTime resolveDepartmentProcessedAt(Ticket ticket) {
		if (ticket.getDepartmentApprovedAt() != null) {
			return ticket.getDepartmentApprovedAt();
		}
		return ticket.getDepartmentRejectedAt();
	}

	private static LocalDateTime resolveProcessedAt(Ticket ticket, AssetReturnTicket assetReturnTicket) {
		if (ticket.getCompletedAt() != null) {
			return ticket.getCompletedAt();
		}
		if (ticket.getCancelledAt() != null) {
			return ticket.getCancelledAt();
		}
		if (assetReturnTicket.getProcessedAt() != null) {
			return assetReturnTicket.getProcessedAt();
		}
		if (assetReturnTicket.getCollectedAt() != null) {
			return assetReturnTicket.getCollectedAt();
		}
		if (ticket.getPurchaseApprovedAt() != null) {
			return ticket.getPurchaseApprovedAt();
		}
		if (ticket.getPurchaseRejectedAt() != null) {
			return ticket.getPurchaseRejectedAt();
		}
		return resolveDepartmentProcessedAt(ticket);
	}

	private static UUID resolveAssetId(AssetReturnTicket assetReturnTicket) {
		if (assetReturnTicket.getTangibleAsset() != null) {
			return assetReturnTicket.getTangibleAsset().getId();
		}
		return assetReturnTicket.getIntangibleAsset().getId();
	}

	private static String resolveAssetCode(AssetReturnTicket assetReturnTicket) {
		if (assetReturnTicket.getTangibleAsset() != null) {
			return assetReturnTicket.getTangibleAsset().getAssetCode();
		}
		return assetReturnTicket.getIntangibleAsset().getAssetCode();
	}

	private static String resolveAssetStatus(AssetReturnTicket assetReturnTicket) {
		if (assetReturnTicket.getTangibleAsset() != null) {
			return assetReturnTicket.getTangibleAsset().getTangibleAssetStatus().name();
		}
		return assetReturnTicket.getIntangibleAsset().getIntangibleAssetStatus().name();
	}

	private static LocalDateTime resolveUsedStartedAt(AssetReturnTicket assetReturnTicket) {
		if (assetReturnTicket.getTangibleAsset() != null) {
			return assetReturnTicket.getTangibleAsset().getUsedStartedAt();
		}
		return assetReturnTicket.getIntangibleAsset().getStartedAt();
	}

	private static List<HistoryItem> createHistories(Ticket ticket, AssetReturnTicket assetReturnTicket) {
		List<HistoryItem> histories = new ArrayList<>();
		histories.add(HistoryItem.of(TicketStatus.REQUESTED.name(), ticket.getCreatedAt()));
		addHistory(histories, TicketStatus.DEPARTMENT_APPROVED.name(), ticket.getDepartmentApprovedAt());
		addHistory(histories, TicketStatus.DEPARTMENT_REJECTED.name(), ticket.getDepartmentRejectedAt());
		addHistory(histories, TicketStatus.ASSET_APPROVED.name(), ticket.getPurchaseApprovedAt());
		addHistory(histories, TicketStatus.ASSET_REJECTED.name(), ticket.getPurchaseRejectedAt());
		addHistory(histories, AssetReturnTicketStatus.COLLECTED.name(), assetReturnTicket.getCollectedAt());
		addHistory(histories, AssetReturnTicketStatus.COMPLETED.name(), assetReturnTicket.getProcessedAt());
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
	public static class RequestDetail {
		private final AssetReturnTargetType assetType;
		private final String categoryName;
		private final String productName;
		private final UUID assetId;
		private final String assetCode;
		private final String assetStatus;
		private final BigDecimal refundAmount;
		private final String reason;

		private static RequestDetail from(Ticket ticket, AssetReturnTicket assetReturnTicket) {
			return RequestDetail.builder()
				.assetType(assetReturnTicket.getTangibleAsset() != null
					? AssetReturnTargetType.TANGIBLE
					: AssetReturnTargetType.INTANGIBLE)
				.categoryName(CategorySummary.from(assetReturnTicket).getName())
				.productName(ItemSummary.from(assetReturnTicket).getName())
				.assetId(resolveAssetId(assetReturnTicket))
				.assetCode(resolveAssetCode(assetReturnTicket))
				.assetStatus(resolveAssetStatus(assetReturnTicket))
				.refundAmount(null)
				.reason(ticket.getRequestReason())
				.build();
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

		private static CategorySummary from(AssetReturnTicket assetReturnTicket) {
			if (assetReturnTicket.getTangibleAsset() != null) {
				TangibleAssetCategory category = assetReturnTicket.getTangibleAsset()
					.getTangibleAssetItem()
					.getTangibleAssetCategory();
				return CategorySummary.builder()
					.categoryId(category.getId())
					.name(category.getName())
					.build();
			}

			IntangibleAssetCategory category = assetReturnTicket.getIntangibleAsset()
				.getIntangibleAssetItem()
				.getIntangibleAssetCategory();
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
		private final String provider;

		private static ItemSummary from(AssetReturnTicket assetReturnTicket) {
			TangibleAsset tangibleAsset = assetReturnTicket.getTangibleAsset();
			if (tangibleAsset != null) {
				TangibleAssetItem item = tangibleAsset.getTangibleAssetItem();
				return ItemSummary.builder()
					.itemId(item.getId())
					.name(item.getProductName())
					.manufacturer(item.getManufacturer())
					.modelName(item.getModelName())
					.build();
			}

			IntangibleAsset intangibleAsset = assetReturnTicket.getIntangibleAsset();
			IntangibleAssetItem item = intangibleAsset.getIntangibleAssetItem();
			return ItemSummary.builder()
				.itemId(item.getId())
				.name(item.getProductName())
				.provider(item.getProvider())
				.build();
		}
	}

	@Getter
	@Builder
	public static class ViewOptions {
		private final boolean showDetailStatus;
		private final boolean showDepartmentApprovalActions;
		private final boolean showAssetActions;
		private final boolean showAssetRecoveryFields;

		private static ViewOptions from(boolean requesterView) {
			return ViewOptions.builder()
				.showDetailStatus(true)
				.showDepartmentApprovalActions(!requesterView)
				.showAssetActions(!requesterView)
				.showAssetRecoveryFields(!requesterView)
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
		private final boolean canCollectAsset;
		private final boolean canCompleteReturn;
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
