package com.ieumsae.assetieum.domain.ticket.purchaserequest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.type.LicenseType;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchasePlanItemStatus;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketAssignmentTargetResponse;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.DirectPurchaseResult;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.ConfirmationStatus;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.PurchaseRequestTicketStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseRequestTicketDetailResponse {

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
	private final PurchaseRequestTicketStatus detailStatus;
	private final String departmentRejectionReason;
	private final String purchaseRejectionReason;
	private final UUID linkedPurchaseId;
	private final MemberSummary assetAssignee;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime assetProcessedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime processedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime completedAt;
	private final RequestedUsageType requestedUsageType;
	private final RequestMethod requestMethod;
	private final AssetType assetType;
	private final Boolean isStandard;
	private final UUID assetItemId;
	private final CategorySummary assetCategory;
	private final String requestedItemDetail;
	private final String manufacturer;
	private final LicenseType licenseType;
	private final String purchaseUrl;
	private final int quantity;
	private final Integer seatCount;
	private final List<TicketAssignmentTargetResponse> assignmentTargets;
	private final BigDecimal expectedPrice;
	private final BigDecimal expectedTotalPrice;   // 예상 합계금액 (단가 × 수량)
	private final BigDecimal actualPrice;
	private final BigDecimal priceDifference;      // 차액 (실제 - 예상합계)
	private final String proofFileUrl;             // 증빙 파일 URL
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime proofFileUploadedAt; // 증빙 파일 업로드 일시
	private final ConfirmationStatus confirmationStatus; // 확인 상태
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime orderedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime deliveryConfirmedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime registeredAt;
	private final MemberRole viewerRole;
	private final ViewOptions viewOptions;
	private final Actions actions;
	private final List<HistoryItem> histories;

	public static PurchaseRequestTicketDetailResponse from(
		Ticket ticket,
		PurchaseRequestTicket purchaseRequestTicket,
		MemberRole viewerRole,
		boolean requesterView,
		UUID linkedPurchaseId,
		PurchasePlanItem linkedPurchasePlanItem,
		DirectPurchaseResult directPurchaseResult,
		List<TicketAssignmentTargetResponse> assignmentTargets,
		Actions actions
	) {
		return PurchaseRequestTicketDetailResponse.builder()
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
			.detailStatus(requesterView ? purchaseRequestTicket.getStatus() : null)
			.departmentRejectionReason(ticket.getDepartmentRejectionReason())
			.purchaseRejectionReason(ticket.getPurchaseRejectionReason())
			.linkedPurchaseId(requesterView ? null : linkedPurchaseId)
			.assetAssignee(MemberSummary.from(ticket.getAssignee()))
			.assetProcessedAt(resolveAssetProcessedAt(ticket))
			.processedAt(ticket.getUpdatedAt())
			.completedAt(ticket.getCompletedAt())
			.requestedUsageType(resolveRequestedUsageType(purchaseRequestTicket))
			.requestMethod(purchaseRequestTicket.getRequestMethod())
			.assetType(resolveAssetType(purchaseRequestTicket))
			.isStandard(purchaseRequestTicket.getIsStandard())
			.assetItemId(resolveAssetItemId(purchaseRequestTicket))
			.assetCategory(CategorySummary.from(purchaseRequestTicket))
			.requestedItemDetail(purchaseRequestTicket.getRequestedItemDetail())
			.manufacturer(purchaseRequestTicket.getManufacturer())
			.licenseType(purchaseRequestTicket.getLicenseType())
			.purchaseUrl(purchaseRequestTicket.getPurchaseUrl())
			.quantity(purchaseRequestTicket.getQuantity())
			.seatCount(purchaseRequestTicket.getSeatCount())
			.assignmentTargets(assignmentTargets)
			.expectedPrice(purchaseRequestTicket.getExpectedPrice())
			.expectedTotalPrice(resolveExpectedTotalPrice(purchaseRequestTicket))
			.actualPrice(resolveActualPrice(linkedPurchasePlanItem, directPurchaseResult))
			.priceDifference(resolvePriceDifference(purchaseRequestTicket, linkedPurchasePlanItem, directPurchaseResult))
			.proofFileUrl(directPurchaseResult != null ? directPurchaseResult.getProofFileUrl() : null)
			.proofFileUploadedAt(directPurchaseResult != null ? directPurchaseResult.getProofFileUploadedAt() : null)
			.confirmationStatus(directPurchaseResult != null ? directPurchaseResult.getConfirmationStatus() : null)
			.orderedAt(resolveOrderedAt(linkedPurchasePlanItem))
			.deliveryConfirmedAt(resolveDeliveryConfirmedAt(linkedPurchasePlanItem))
			.registeredAt(resolveRegisteredAt(linkedPurchasePlanItem, directPurchaseResult))
			.viewerRole(viewerRole)
			.viewOptions(ViewOptions.from(requesterView))
			.actions(actions)
			.histories(createHistories(ticket))
			.build();
	}

	private static BigDecimal resolveActualPrice(
		PurchasePlanItem linkedPurchasePlanItem,
		DirectPurchaseResult directPurchaseResult
	) {
		if (directPurchaseResult != null) {
			return directPurchaseResult.getActualPrice();
		}
		if (linkedPurchasePlanItem == null || linkedPurchasePlanItem.getActualUnitPrice() == null) {
			return null;
		}
		return linkedPurchasePlanItem.getActualUnitPrice()
			.multiply(BigDecimal.valueOf(linkedPurchasePlanItem.getQuantity()));
	}

	private static BigDecimal resolveExpectedTotalPrice(PurchaseRequestTicket ticket) {
		if (ticket.getExpectedPrice() == null) {
			return null;
		}
		return ticket.getExpectedPrice().multiply(BigDecimal.valueOf(ticket.getQuantity()));
	}

	private static BigDecimal resolvePriceDifference(
		PurchaseRequestTicket ticket,
		PurchasePlanItem linkedPurchasePlanItem,
		DirectPurchaseResult directPurchaseResult
	) {
		BigDecimal actualPrice = resolveActualPrice(linkedPurchasePlanItem, directPurchaseResult);
		BigDecimal expectedTotal = resolveExpectedTotalPrice(ticket);
		if (actualPrice == null || expectedTotal == null) {
			return null;
		}
		return actualPrice.subtract(expectedTotal);
	}

	private static LocalDateTime resolveOrderedAt(PurchasePlanItem linkedPurchasePlanItem) {
		if (linkedPurchasePlanItem == null) {
			return null;
		}
		return linkedPurchasePlanItem.getPurchasePlan().getOrderedAt();
	}

	private static LocalDateTime resolveDeliveryConfirmedAt(PurchasePlanItem linkedPurchasePlanItem) {
		if (linkedPurchasePlanItem == null) {
			return null;
		}
		return linkedPurchasePlanItem.getReceivedAt();
	}

	private static LocalDateTime resolveRegisteredAt(
		PurchasePlanItem linkedPurchasePlanItem,
		DirectPurchaseResult directPurchaseResult
	) {
		if (directPurchaseResult != null) {
			return directPurchaseResult.getCreatedAt();
		}
		if (linkedPurchasePlanItem == null
			|| linkedPurchasePlanItem.getPurchasePlanItemStatus() != PurchasePlanItemStatus.ASSET_REGISTERED) {
			return null;
		}
		return linkedPurchasePlanItem.getUpdatedAt();
	}

	private static AssetType resolveAssetType(PurchaseRequestTicket ticket) {
		if (ticket.getTangibleAssetCategory() != null || ticket.getTangibleAssetItem() != null) {
			return AssetType.TANGIBLE;
		}
		return AssetType.INTANGIBLE;
	}

	private static RequestedUsageType resolveRequestedUsageType(PurchaseRequestTicket ticket) {
		if (resolveAssetType(ticket) == AssetType.INTANGIBLE) {
			return null;
		}
		return ticket.getRequestedUsageType();
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
		addHistory(histories, TicketStatus.IN_PROGRESS.name(), ticket.getUpdatedAt());
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

		private static CategorySummary from(PurchaseRequestTicket ticket) {
			TangibleAssetCategory tangibleCategory = ticket.getTangibleAssetCategory();
			if (tangibleCategory != null) {
				return CategorySummary.builder()
					.categoryId(tangibleCategory.getId())
					.name(tangibleCategory.getName())
					.build();
			}

			IntangibleAssetCategory intangibleCategory = ticket.getIntangibleAssetCategory();
			if (intangibleCategory != null) {
				return CategorySummary.builder()
					.categoryId(intangibleCategory.getId())
					.name(intangibleCategory.getName())
					.build();
			}

			TangibleAssetItem tangibleItem = ticket.getTangibleAssetItem();
			if (tangibleItem != null) {
				return CategorySummary.builder()
					.categoryId(tangibleItem.getTangibleAssetCategory().getId())
					.name(tangibleItem.getTangibleAssetCategory().getName())
					.build();
			}

			IntangibleAssetItem intangibleItem = ticket.getIntangibleAssetItem();
			return CategorySummary.builder()
				.categoryId(intangibleItem.getIntangibleAssetCategory().getId())
				.name(intangibleItem.getIntangibleAssetCategory().getName())
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
