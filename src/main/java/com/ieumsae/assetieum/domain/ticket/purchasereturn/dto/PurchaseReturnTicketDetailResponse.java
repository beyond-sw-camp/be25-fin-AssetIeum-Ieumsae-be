package com.ieumsae.assetieum.domain.ticket.purchasereturn.dto;

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
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.entity.PurchaseReturnTicket;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.type.PurchaseReturnTicketStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseReturnTicketDetailResponse {

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
	private final PurchaseReturnTicketStatus detailStatus;
	private final String departmentRejectionReason;
	private final String purchaseRejectionReason;
	private final MemberSummary assetAssignee;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime collectedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime returnProcessedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime processedAt;
	private final AssetReturnTargetType assetType;
	private final CategorySummary assetCategory;
	private final ItemSummary assetItem;
	private final UUID assetId;
	private final String assetCode;
	private final String assetStatus;
	private final String returnReason;
	private final boolean collected;
	private final BigDecimal refundAmount;
	private final RequestDetail requestDetail;
	private final MemberRole viewerRole;
	private final Actions actions;
	private final List<HistoryItem> histories;

	public static PurchaseReturnTicketDetailResponse from(
		Ticket ticket,
		PurchaseReturnTicket purchaseReturnTicket,
		MemberRole viewerRole,
		Actions actions
	) {
		return PurchaseReturnTicketDetailResponse.builder()
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
			.detailStatus(purchaseReturnTicket.getStatus())
			.departmentRejectionReason(ticket.getDepartmentRejectionReason())
			.purchaseRejectionReason(ticket.getPurchaseRejectionReason())
			.assetAssignee(MemberSummary.from(ticket.getAssignee()))
			.collectedAt(purchaseReturnTicket.getCollectedAt())
			.returnProcessedAt(purchaseReturnTicket.getShippedAt())
			.processedAt(resolveProcessedAt(ticket, purchaseReturnTicket))
			.assetType(resolveAssetType(purchaseReturnTicket))
			.assetCategory(CategorySummary.from(purchaseReturnTicket))
			.assetItem(ItemSummary.from(purchaseReturnTicket))
			.assetId(resolveAssetId(purchaseReturnTicket))
			.assetCode(resolveAssetCode(purchaseReturnTicket))
			.assetStatus(resolveAssetStatus(purchaseReturnTicket))
			.returnReason(ticket.getRequestReason())
			.collected(purchaseReturnTicket.getCollectedAt() != null)
			.refundAmount(resolveRefundAmount(purchaseReturnTicket))
			.requestDetail(RequestDetail.from(ticket, purchaseReturnTicket))
			.viewerRole(viewerRole)
			.actions(actions)
			.histories(createHistories(ticket, purchaseReturnTicket))
			.build();
	}

	private static LocalDateTime resolveDepartmentProcessedAt(Ticket ticket) {
		if (ticket.getDepartmentApprovedAt() != null) {
			return ticket.getDepartmentApprovedAt();
		}
		return ticket.getDepartmentRejectedAt();
	}

	private static LocalDateTime resolveProcessedAt(Ticket ticket, PurchaseReturnTicket purchaseReturnTicket) {
		if (ticket.getCompletedAt() != null) {
			return ticket.getCompletedAt();
		}
		if (ticket.getCancelledAt() != null) {
			return ticket.getCancelledAt();
		}
		if (purchaseReturnTicket.getShippedAt() != null) {
			return purchaseReturnTicket.getShippedAt();
		}
		if (purchaseReturnTicket.getCollectedAt() != null) {
			return purchaseReturnTicket.getCollectedAt();
		}
		if (ticket.getPurchaseApprovedAt() != null) {
			return ticket.getPurchaseApprovedAt();
		}
		if (ticket.getPurchaseRejectedAt() != null) {
			return ticket.getPurchaseRejectedAt();
		}
		return resolveDepartmentProcessedAt(ticket);
	}

	private static AssetReturnTargetType resolveAssetType(PurchaseReturnTicket purchaseReturnTicket) {
		return purchaseReturnTicket.getTangibleAsset() != null
			? AssetReturnTargetType.TANGIBLE
			: AssetReturnTargetType.INTANGIBLE;
	}

	private static UUID resolveAssetId(PurchaseReturnTicket purchaseReturnTicket) {
		if (purchaseReturnTicket.getTangibleAsset() != null) {
			return purchaseReturnTicket.getTangibleAsset().getId();
		}
		return purchaseReturnTicket.getIntangibleAsset().getId();
	}

	private static String resolveAssetCode(PurchaseReturnTicket purchaseReturnTicket) {
		if (purchaseReturnTicket.getTangibleAsset() != null) {
			return purchaseReturnTicket.getTangibleAsset().getAssetCode();
		}
		return purchaseReturnTicket.getIntangibleAsset().getAssetCode();
	}

	private static String resolveAssetStatus(PurchaseReturnTicket purchaseReturnTicket) {
		if (purchaseReturnTicket.getTangibleAsset() != null) {
			return purchaseReturnTicket.getTangibleAsset().getTangibleAssetStatus().name();
		}
		return purchaseReturnTicket.getIntangibleAsset().getIntangibleAssetStatus().name();
	}

	private static List<HistoryItem> createHistories(Ticket ticket, PurchaseReturnTicket purchaseReturnTicket) {
		List<HistoryItem> histories = new ArrayList<>();
		histories.add(HistoryItem.of(TicketStatus.REQUESTED.name(), ticket.getCreatedAt()));
		addHistory(histories, TicketStatus.DEPARTMENT_APPROVED.name(), ticket.getDepartmentApprovedAt());
		addHistory(histories, TicketStatus.DEPARTMENT_REJECTED.name(), ticket.getDepartmentRejectedAt());
		addHistory(histories, TicketStatus.ASSET_APPROVED.name(), ticket.getPurchaseApprovedAt());
		addHistory(histories, TicketStatus.ASSET_REJECTED.name(), ticket.getPurchaseRejectedAt());
		addHistory(histories, PurchaseReturnTicketStatus.COLLECTED.name(), purchaseReturnTicket.getCollectedAt());
		addHistory(histories, PurchaseReturnTicketStatus.COMPLETED.name(), purchaseReturnTicket.getShippedAt());
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
		private final String returnReason;
		private final BigDecimal refundAmount;

		private static RequestDetail from(Ticket ticket, PurchaseReturnTicket purchaseReturnTicket) {
			return RequestDetail.builder()
				.assetType(resolveAssetType(purchaseReturnTicket))
				.categoryName(CategorySummary.from(purchaseReturnTicket).getName())
				.productName(ItemSummary.from(purchaseReturnTicket).getName())
				.assetId(resolveAssetId(purchaseReturnTicket))
				.assetCode(resolveAssetCode(purchaseReturnTicket))
				.assetStatus(resolveAssetStatus(purchaseReturnTicket))
				.returnReason(ticket.getRequestReason())
				.refundAmount(resolveRefundAmount(purchaseReturnTicket))
				.build();
		}
	}

	private static BigDecimal resolveRefundAmount(PurchaseReturnTicket purchaseReturnTicket) {
		if (purchaseReturnTicket.getTangibleAsset() != null) {
			return purchaseReturnTicket.getTangibleAsset().getPurchasePrice();
		}
		return purchaseReturnTicket.getIntangibleAsset().getPurchasePrice();
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

		private static CategorySummary from(PurchaseReturnTicket purchaseReturnTicket) {
			if (purchaseReturnTicket.getTangibleAsset() != null) {
				TangibleAssetCategory category = purchaseReturnTicket.getTangibleAsset()
					.getTangibleAssetItem()
					.getTangibleAssetCategory();
				return CategorySummary.builder()
					.categoryId(category.getId())
					.name(category.getName())
					.build();
			}

			IntangibleAssetCategory category = purchaseReturnTicket.getIntangibleAsset()
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

		private static ItemSummary from(PurchaseReturnTicket purchaseReturnTicket) {
			TangibleAsset tangibleAsset = purchaseReturnTicket.getTangibleAsset();
			if (tangibleAsset != null) {
				TangibleAssetItem item = tangibleAsset.getTangibleAssetItem();
				return ItemSummary.builder()
					.itemId(item.getId())
					.name(item.getProductName())
					.manufacturer(item.getManufacturer())
					.modelName(item.getModelName())
					.build();
			}

			IntangibleAsset intangibleAsset = purchaseReturnTicket.getIntangibleAsset();
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
