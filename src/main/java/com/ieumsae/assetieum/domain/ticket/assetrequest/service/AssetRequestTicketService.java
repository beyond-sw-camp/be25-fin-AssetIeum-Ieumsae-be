package com.ieumsae.assetieum.domain.ticket.assetrequest.service;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.repository.PurchasePlanItemRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignableItemSearchRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignableItemsResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.assetrequest.repository.AssetRequestTicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketAssignmentTargetResponse;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketAssignmentTargetService;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketRequesterResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketService;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetRequestTicketService {

	private static final String HR_EVENT_ONBOARDING_REQUEST_REASON = "입사 자산 신청";

	private final TicketRepository ticketRepository;
	private final AssetRequestTicketRepository assetRequestTicketRepository;
	private final TangibleAssetItemRepository tangibleAssetItemRepository;
	private final IntangibleAssetItemRepository intangibleAssetItemRepository;
	private final TangibleAssetRepository tangibleAssetRepository;
	private final IntangibleAssetRepository intangibleAssetRepository;
	private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TicketRequesterResolver ticketRequesterResolver;
	private final TicketService ticketService;
	private final AssetRequestAssignmentService assetRequestAssignmentService;
	private final AssetRequestAvailabilityService assetRequestAvailabilityService;
	private final AssetRequestActionResolver assetRequestActionResolver;
	private final TicketAssignmentTargetService ticketAssignmentTargetService;
	private final PurchasePlanItemRepository purchasePlanItemRepository;
	private final NotificationService notificationService;

	@Transactional
	public AssetRequestTicketCreateResponse createAssetRequestTicket(
		AuthenticatedMember authenticatedMember,
		AssetRequestTicketCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Member approver = ticketApprovalResolver.resolveDepartmentApprover(requester);
		TangibleAssetItem tangibleAssetItem = null;
		IntangibleAssetItem intangibleAssetItem = null;
		RequestedUsageType requestedUsageType = resolveRequestedUsageType(
			request.getAssetType(),
			request.getRequestedUsageType()
		);

		if (request.getAssetType() == AssetType.TANGIBLE) {
			tangibleAssetItem = findTangibleAssetItem(request.getAssetItemId(), companyId);
			validateNonStandardTangibleInventory(tangibleAssetItem, request.getQuantity(), companyId);
		} else {
			intangibleAssetItem = findIntangibleAssetItem(
				request.getAssetItemId(),
				companyId
			);
			validateIntangibleAssignmentTargets(request.getAssignmentTargetMemberIds());
			validateNonStandardIntangibleInventory(intangibleAssetItem, request.getQuantity(), companyId, requester.getDepartment().getId());
		}
		BigDecimal estimatedUnitPrice = resolveEstimatedUnitPrice(
			request,
			tangibleAssetItem,
			intangibleAssetItem,
			companyId,
			requester.getDepartment().getId()
		);

		Ticket ticket = ticketRepository.save(Ticket.createAssetRequest(
			requester.getCompany(),
			ticketNoGenerator.generate(companyId),
			requester,
			requester.getDepartment(),
			approver,
			normalize(request.getRequestReason())
		));

		AssetRequestTicket assetRequestTicket = assetRequestTicketRepository.save(
			AssetRequestTicket.createRequest(
				ticket,
				requester.getCompany(),
				requestedUsageType,
				tangibleAssetItem,
				intangibleAssetItem,
				request.getQuantity(),
				estimatedUnitPrice
			)
		);
		ticketAssignmentTargetService.saveRequiredTargets(
			companyId,
			ticket,
			request.getAssignmentTargetMemberIds(),
			request.getQuantity(),
			requestedUsageType,
			request.getAssetType() == AssetType.TANGIBLE
		);
		notifyTicketApprover(ticket, "자산 요청이 접수되었습니다.", "자산 요청을 확인하고 승인 여부를 처리하세요.");

		return AssetRequestTicketCreateResponse.from(
			ticket,
			assetRequestTicket,
			request.getAssetType(),
			request.getAssetItemId()
		);
	}

	@Transactional
	public void approveDueOnboardingAssetRequestTickets(LocalDate executionDate) {
		LocalDateTime startInclusive = executionDate.atStartOfDay();
		LocalDateTime endExclusive = startInclusive.plusDays(1);

		List<AssetRequestTicket> tickets = assetRequestTicketRepository
			.findAllByTicket_RequestReasonAndTicket_TicketStatusAndTicket_CreatedAtGreaterThanEqualAndTicket_CreatedAtLessThanAndDeletedAtIsNullOrderByTicket_CreatedAtAsc(
				HR_EVENT_ONBOARDING_REQUEST_REASON,
				TicketStatus.REQUESTED,
				startInclusive,
				endExclusive
			);

		for (AssetRequestTicket assetRequestTicket : tickets) {
			ticketService.approveDepartmentForHrEvent(
				assetRequestTicket.getCompany().getId(),
				assetRequestTicket.getTicket().getId()
			);
		}
	}

	public AssetRequestTicketDetailResponse getAssetRequestTicket(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		AssetRequestTicket assetRequestTicket = findAssetRequestTicket(ticketId, companyId);
		Ticket ticket = assetRequestTicket.getTicket();

		assetRequestActionResolver.validateReadable(ticket, viewer);
		boolean requesterView = ticket.getRequester().getId().equals(viewer.getId());

		return AssetRequestTicketDetailResponse.from(
			ticket,
			assetRequestTicket,
			viewer.getRole(),
			requesterView,
			resolveLinkedPurchasePlanId(companyId, ticket),
			getAssignmentTargetResponses(companyId, ticket),
			assetRequestActionResolver.createActions(ticket, viewer)
		);
	}

	private UUID resolveLinkedPurchasePlanId(UUID companyId, Ticket ticket) {
		// 반려/취소된 구매계획 연결은 이력으로만 남기고, 현재 유효한 구매계획만 상세 이동 대상으로 사용한다.
		return purchasePlanItemRepository
			.findFirstByTicket_IdAndCompany_IdAndPurchasePlan_PurchaseRequestStatusNotInOrderByIdDesc(
				ticket.getId(),
				companyId,
				List.of(PurchaseRequestStatus.REJECTED, PurchaseRequestStatus.CANCELLED)
			)
			.map(purchasePlanItem -> purchasePlanItem.getPurchasePlan().getId())
			.orElse(null);
	}

	private void notifyTicketApprover(Ticket ticket, String title, String content) {
		Member approver = ticket.getApprover();
		if (approver == null || !approver.isActive()) {
			return;
		}

		notificationService.createNotification(
			approver,
			NotificationType.TICKET_STATUS_CHANGED,
			title,
			content,
			NotificationTargetType.TICKET,
			ticket.getId()
		);
	}

	private List<TicketAssignmentTargetResponse> getAssignmentTargetResponses(UUID companyId, Ticket ticket) {
		return ticketAssignmentTargetService.findTargets(companyId, ticket).stream()
			.map(TicketAssignmentTargetResponse::from)
			.toList();
	}

	private RequestedUsageType resolveRequestedUsageType(AssetType assetType, RequestedUsageType requestedUsageType) {
		if (assetType == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자산 유형은 필수입니다.");
		}
		if (assetType == AssetType.INTANGIBLE) {
			return RequestedUsageType.PERSONAL;
		}
		if (requestedUsageType == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형자산은 요청 용도가 필수입니다.");
		}
		return requestedUsageType;
	}

	public AssetRequestAssignableItemsResponse getAssignableItems(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		AssetRequestAssignableItemSearchRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		AssetRequestTicket assetRequestTicket = findAssetRequestTicket(ticketId, companyId);
		Ticket ticket = assetRequestTicket.getTicket();

		assetRequestActionResolver.validateReadable(ticket, viewer);
		return assetRequestAvailabilityService.getAssignableItems(companyId, assetRequestTicket, request);
	}

	@Transactional
	public AssetRequestAssignResponse assignAssetRequest(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		AssetRequestAssignRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member assignee = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		AssetRequestTicket assetRequestTicket = findAssetRequestTicket(ticketId, companyId);

		return assetRequestAssignmentService.assign(companyId, assignee, assetRequestTicket, request);
	}

	private TangibleAssetItem findTangibleAssetItem(UUID itemId, UUID companyId) {
		TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

		if (!item.getCompany().getId().equals(companyId)) {
			throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND);
		}
		return item;
	}

	private IntangibleAssetItem findIntangibleAssetItem(UUID itemId, UUID companyId) {
		IntangibleAssetItem item = intangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));

		if (!item.getCompany().getId().equals(companyId)) {
			throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND);
		}
		return item;
	}

	private AssetRequestTicket findAssetRequestTicket(UUID ticketId, UUID companyId) {
		return assetRequestTicketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
	}

	private void validateNonStandardTangibleInventory(TangibleAssetItem item, int quantity, UUID companyId) {
		if (Boolean.TRUE.equals(item.getIsStandard())) {
			return;
		}

		long availableCount = tangibleAssetRepository.countByCompany_IdAndTangibleAssetItem_IdAndTangibleAssetStatus(
			companyId,
			item.getId(),
			TangibleAssetStatus.AVAILABLE
		);
		if (availableCount < quantity) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비표준 유형자산 요청은 재고가 충분한 품목만 요청할 수 있습니다.");
		}
	}

	private BigDecimal resolveEstimatedUnitPrice(
		AssetRequestTicketCreateRequest request,
		TangibleAssetItem tangibleAssetItem,
		IntangibleAssetItem intangibleAssetItem,
		UUID companyId,
		UUID requesterDepartmentId
	) {
		int availableCount = tangibleAssetItem != null
			? getAvailableTangibleAssetCount(companyId, tangibleAssetItem.getId())
			: getAvailableIntangibleSeatCount(companyId, intangibleAssetItem.getId(), requesterDepartmentId);

		if (availableCount >= request.getQuantity()) {
			return null;
		}

		boolean standard = tangibleAssetItem != null
			? Boolean.TRUE.equals(tangibleAssetItem.getIsStandard())
			: Boolean.TRUE.equals(intangibleAssetItem.getIsStandard());
		if (!standard) {
			return null;
		}
		if (request.getEstimatedUnitPrice() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "가용 재고가 부족한 표준 자산 요청은 구매 예상 단가가 필수입니다.");
		}
		return request.getEstimatedUnitPrice();
	}

	private int getAvailableTangibleAssetCount(UUID companyId, UUID itemId) {
		return Math.toIntExact(tangibleAssetRepository.countByCompany_IdAndTangibleAssetItem_IdAndTangibleAssetStatus(
			companyId,
			itemId,
			TangibleAssetStatus.AVAILABLE
		));
	}

	private void validateIntangibleAssignmentTargets(List<UUID> assignmentTargetMemberIds) {
		if (assignmentTargetMemberIds == null || assignmentTargetMemberIds.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산은 배정 대상자를 1명 이상 입력해야 합니다.");
		}
	}

	private void validateNonStandardIntangibleInventory(IntangibleAssetItem item, int quantity, UUID companyId, UUID requesterDepartmentId) {
		if (Boolean.TRUE.equals(item.getIsStandard())) {
			return;
		}

		if (getAvailableIntangibleSeatCount(companyId, item.getId(), requesterDepartmentId) < quantity) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비표준 무형자산 요청은 재고 좌석이 충분한 품목만 요청할 수 있습니다.");
		}
	}

	private int getAvailableIntangibleSeatCount(UUID companyId, UUID itemId, UUID requesterDepartmentId) {
		List<IntangibleAsset> assets = intangibleAssetRepository.findAllByCompany_IdAndIntangibleAssetItem_IdAndIntangibleAssetStatusIn(
			companyId,
			itemId,
			List.of(IntangibleAssetStatus.AVAILABLE, IntangibleAssetStatus.IN_USE)
		);

		int availableSeatCount = 0;
		for (IntangibleAsset asset : assets) {
			if (asset.getDepartment() != null
				&& !asset.getDepartment().getId().equals(requesterDepartmentId)) {
				continue;
			}
			long activeAssignmentCount = intangibleAssetAssignmentRepository
				.countByCompany_IdAndIntangibleAsset_IdAndAssignmentStatus(
					companyId,
					asset.getId(),
					AssignmentStatus.ACTIVE
				);
			availableSeatCount += Math.max(asset.getSeatCount() - Math.toIntExact(activeAssignmentCount), 0);
		}
		return availableSeatCount;
	}

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
