package com.ieumsae.assetieum.domain.ticket.purchasereturn.service;

import com.ieumsae.assetieum.domain.budget.budget.service.BudgetExecutionService;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.AssignedAssetValidator;
import com.ieumsae.assetieum.domain.ticket.common.service.IntangibleAssetTicketConflictValidator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketRequesterResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TangibleAssetTicketConflictValidator;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnAvailableAssetResponse;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnAvailableAssetSearchRequest;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnCollectResponse;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnCompleteResponse;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.entity.PurchaseReturnTicket;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.repository.PurchaseReturnTicketRepository;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.type.PurchaseReturnTicketStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseReturnTicketService {

	private final TicketRepository ticketRepository;
	private final PurchaseReturnTicketRepository purchaseReturnTicketRepository;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TicketRequesterResolver ticketRequesterResolver;
	private final AssignedAssetValidator assignedAssetValidator;
	private final TangibleAssetTicketConflictValidator tangibleAssetTicketConflictValidator;
	private final IntangibleAssetTicketConflictValidator intangibleAssetTicketConflictValidator;
	private final BudgetExecutionService budgetExecutionService;
	private final NotificationService notificationService;

	public List<PurchaseReturnAvailableAssetResponse> getAvailableAssets(
		AuthenticatedMember authenticatedMember,
		PurchaseReturnAvailableAssetSearchRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);

		if (request.getAssetType() == AssetReturnTargetType.TANGIBLE) {
			return tangibleAssetAssignmentRepository.findAllByCompany_IdAndMember_IdAndAssignmentStatus(
					companyId,
					requester.getId(),
					AssignmentStatus.ACTIVE
				)
				.stream()
				.filter(assignedAssetValidator::isTangibleInUseByAssignee)
				.map(PurchaseReturnAvailableAssetResponse::from)
				.toList();
		}

		return intangibleAssetAssignmentRepository.findAllByCompany_IdAndMember_IdAndAssignmentStatus(
				companyId,
				requester.getId(),
				com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus.ACTIVE
			)
			.stream()
			.filter(assignedAssetValidator::isIntangibleInUseByAssignee)
			.map(PurchaseReturnAvailableAssetResponse::from)
			.toList();
	}

	public PurchaseReturnTicketDetailResponse getPurchaseReturnTicket(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		PurchaseReturnTicket purchaseReturnTicket = findPurchaseReturnTicket(ticketId, companyId);
		Ticket ticket = purchaseReturnTicket.getTicket();

		validateReadable(ticket, viewer);

		return PurchaseReturnTicketDetailResponse.from(
			ticket,
			purchaseReturnTicket,
			viewer.getRole(),
			createActions(ticket, purchaseReturnTicket, viewer)
		);
	}

	@Transactional
	public PurchaseReturnTicketCreateResponse createPurchaseReturnTicket(
		AuthenticatedMember authenticatedMember,
		PurchaseReturnTicketCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);

		if (request.getAssetType() == AssetReturnTargetType.TANGIBLE) {
			return createTangiblePurchaseReturnTicket(companyId, requester, request);
		}

		return createIntangiblePurchaseReturnTicket(companyId, requester, request);
	}

	@Transactional
	public PurchaseReturnCollectResponse collectPurchaseReturn(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member collector = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		PurchaseReturnTicket purchaseReturnTicket = findPurchaseReturnTicket(ticketId, companyId);

		validateCollectable(ticket, purchaseReturnTicket, collector);

		LocalDateTime collectedAt = LocalDateTime.now();
		// 반품 회수 시 배정 이력을 종료하고 자산을 회수 완료 상태로 전환한다.
		endActiveAssignment(purchaseReturnTicket, companyId, collectedAt);
		markAssetCollected(purchaseReturnTicket);
		purchaseReturnTicket.collect(collectedAt);

		return PurchaseReturnCollectResponse.from(ticket, purchaseReturnTicket);
	}

	@Transactional
	public PurchaseReturnCompleteResponse completePurchaseReturn(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member processor = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		PurchaseReturnTicket purchaseReturnTicket = findPurchaseReturnTicket(ticketId, companyId);

		validateCompletable(ticket, purchaseReturnTicket, processor);

		LocalDateTime processedAt = LocalDateTime.now();
		// 반품 완료 시 별도 환불금액 입력 없이 자산 구매가 기준으로 사용 예산을 회복한다.
		budgetExecutionService.recoverForPurchaseReturn(ticket, purchaseReturnTicket);
		if (purchaseReturnTicket.getTangibleAsset() != null) {
			purchaseReturnTicket.getTangibleAsset().dispose();
		}
		purchaseReturnTicket.complete(processedAt);
		ticket.changeProcessingStatus(TicketStatus.COMPLETED, processedAt);

		return PurchaseReturnCompleteResponse.from(ticket, purchaseReturnTicket);
	}

	private PurchaseReturnTicketCreateResponse createTangiblePurchaseReturnTicket(
		UUID companyId,
		Member requester,
		PurchaseReturnTicketCreateRequest request
	) {
		TangibleAssetAssignment assignment = tangibleAssetAssignmentRepository
			.findByIdAndCompany_IdAndMember_IdAndAssignmentStatus(
				request.getAssignmentId(),
				companyId,
				requester.getId(),
				AssignmentStatus.ACTIVE
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용 중인 유형자산 배정을 찾을 수 없습니다."));

		validateTangibleReturnTarget(assignment, requester);
		tangibleAssetTicketConflictValidator.validateNoOngoingTangibleAssetTicket(
			companyId,
			assignment.getTangibleAsset().getId()
		);

		Ticket ticket = createCommonTicket(companyId, requester, request.getRequestReason());
		PurchaseReturnTicket purchaseReturnTicket = purchaseReturnTicketRepository.save(
			PurchaseReturnTicket.createTangibleReturn(ticket, requester.getCompany(), assignment.getTangibleAsset())
		);
		// 유형자산은 반품 요청 중임을 자산 상태에도 표시한다.
		assignment.getTangibleAsset().requestReturn();
		notifyMember(ticket.getApprover(), "자산 반납 요청이 접수되었습니다.", "자산 반납 요청을 확인하고 승인 여부를 처리하세요.", ticket);

		return PurchaseReturnTicketCreateResponse.from(ticket, purchaseReturnTicket);
	}

	private PurchaseReturnTicketCreateResponse createIntangiblePurchaseReturnTicket(
		UUID companyId,
		Member requester,
		PurchaseReturnTicketCreateRequest request
	) {
		IntangibleAssetAssignment assignment = intangibleAssetAssignmentRepository
			.findByIdAndCompany_IdAndMember_IdAndAssignmentStatus(
				request.getAssignmentId(),
				companyId,
				requester.getId(),
				com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus.ACTIVE
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용 중인 무형자산 배정을 찾을 수 없습니다."));

		validateIntangibleReturnTarget(assignment, requester);
		intangibleAssetTicketConflictValidator.validateNoOngoingIntangibleAssetReturnTicket(
			companyId,
			assignment.getIntangibleAsset().getId()
		);

		Ticket ticket = createCommonTicket(companyId, requester, request.getRequestReason());
		PurchaseReturnTicket purchaseReturnTicket = purchaseReturnTicketRepository.save(
			PurchaseReturnTicket.createIntangibleReturn(ticket, requester.getCompany(), assignment.getIntangibleAsset())
		);
		notifyMember(ticket.getApprover(), "자산 반납 요청이 접수되었습니다.", "자산 반납 요청을 확인하고 승인 여부를 처리하세요.", ticket);

		return PurchaseReturnTicketCreateResponse.from(ticket, purchaseReturnTicket);
	}

	private Ticket createCommonTicket(UUID companyId, Member requester, String requestReason) {
		Member approver = ticketApprovalResolver.resolveDepartmentApprover(requester);

		return ticketRepository.save(Ticket.createPurchaseReturn(
			requester.getCompany(),
			ticketNoGenerator.generate(companyId),
			requester,
			requester.getDepartment(),
			approver,
			requestReason.trim()
		));
	}

	private void validateTangibleReturnTarget(TangibleAssetAssignment assignment, Member requester) {
		if (!assignedAssetValidator.isTangibleInUseByAssignee(assignment)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "반품 요청 가능한 유형자산이 아닙니다.");
		}

		assignedAssetValidator.validateTangibleRequester(assignment, requester);
	}

	private void validateIntangibleReturnTarget(IntangibleAssetAssignment assignment, Member requester) {
		if (!assignedAssetValidator.isIntangibleInUseByAssignee(assignment)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "반품 요청 가능한 무형자산이 아닙니다.");
		}

		assignedAssetValidator.validateIntangibleRequester(assignment, requester);
	}

	private void notifyMember(Member receiver, String title, String content, Ticket ticket) {
		if (receiver == null || !receiver.isActive()) {
			return;
		}

		notificationService.createNotification(
			receiver,
			NotificationType.TICKET_STATUS_CHANGED,
			title,
			content,
			NotificationTargetType.TICKET,
			ticket.getId()
		);
	}

	private PurchaseReturnTicket findPurchaseReturnTicket(UUID ticketId, UUID companyId) {
		return purchaseReturnTicketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
	}

	private void validateReadable(Ticket ticket, Member viewer) {
		if (ticket.getRequester().getId().equals(viewer.getId())
			|| ticket.getApprover().getId().equals(viewer.getId())
			|| isAssetRole(viewer.getRole())) {
			return;
		}
		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}

	private PurchaseReturnTicketDetailResponse.Actions createActions(
		Ticket ticket,
		PurchaseReturnTicket purchaseReturnTicket,
		Member viewer
	) {
		if (ticket.getRequester().getId().equals(viewer.getId())) {
			return noActions();
		}

		boolean departmentApprover = ticket.getApprover().getId().equals(viewer.getId());
		boolean requested = ticket.getTicketStatus() == TicketStatus.REQUESTED;
		boolean departmentApproved = ticket.getTicketStatus() == TicketStatus.DEPARTMENT_APPROVED;
		boolean assetAssignable = isAssetAssignable(ticket, viewer);
		boolean assignee = ticket.getAssignee() != null && ticket.getAssignee().getId().equals(viewer.getId());

		return PurchaseReturnTicketDetailResponse.Actions.builder()
			.canApproveDepartment(departmentApprover && requested)
			.canRejectDepartment(departmentApprover && requested)
			.canAssignAsset(assetAssignable && ticket.getAssignee() == null && (requested || departmentApproved))
			.canApproveAsset(assetAssignable && departmentApproved && assignee)
			.canRejectAsset(assetAssignable && departmentApproved && assignee)
			.canCollectAsset(canCollectAsset(ticket, purchaseReturnTicket, viewer))
			.canCompleteReturn(canCompleteReturn(ticket, purchaseReturnTicket, viewer))
			.build();
	}

	private PurchaseReturnTicketDetailResponse.Actions noActions() {
		return PurchaseReturnTicketDetailResponse.Actions.builder()
			.canApproveDepartment(false)
			.canRejectDepartment(false)
			.canAssignAsset(false)
			.canApproveAsset(false)
			.canRejectAsset(false)
			.canCollectAsset(false)
			.canCompleteReturn(false)
			.build();
	}

	private boolean canCollectAsset(Ticket ticket, PurchaseReturnTicket purchaseReturnTicket, Member viewer) {
		return isAssetRole(viewer.getRole())
			&& ticket.getTicketStatus() == TicketStatus.IN_PROGRESS
			&& purchaseReturnTicket.getStatus() == PurchaseReturnTicketStatus.REQUESTED
			&& ticket.getAssignee() != null
			&& ticket.getAssignee().getId().equals(viewer.getId());
	}

	private boolean canCompleteReturn(Ticket ticket, PurchaseReturnTicket purchaseReturnTicket, Member viewer) {
		return isAssetRole(viewer.getRole())
			&& ticket.getTicketStatus() == TicketStatus.IN_PROGRESS
			&& purchaseReturnTicket.getStatus() == PurchaseReturnTicketStatus.COLLECTED
			&& ticket.getAssignee() != null
			&& ticket.getAssignee().getId().equals(viewer.getId());
	}

	private void validateCollectable(Ticket ticket, PurchaseReturnTicket purchaseReturnTicket, Member collector) {
		if (!canCollectAsset(ticket, purchaseReturnTicket, collector)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회수 가능한 반품/환불 티켓이 아닙니다.");
		}
	}

	private void validateCompletable(Ticket ticket, PurchaseReturnTicket purchaseReturnTicket, Member processor) {
		if (!canCompleteReturn(ticket, purchaseReturnTicket, processor)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "반품 처리 가능한 반품/환불 티켓이 아닙니다.");
		}
	}

	private void endActiveAssignment(PurchaseReturnTicket purchaseReturnTicket, UUID companyId, LocalDateTime endedAt) {
		if (purchaseReturnTicket.getTangibleAsset() != null) {
			tangibleAssetAssignmentRepository.findByCompany_IdAndTangibleAsset_IdAndAssignmentStatus(
					companyId,
					purchaseReturnTicket.getTangibleAsset().getId(),
					AssignmentStatus.ACTIVE
				)
				.ifPresent(assignment -> assignment.end(endedAt));
			return;
		}

		intangibleAssetAssignmentRepository.findAllByCompany_IdAndIntangibleAsset_IdAndAssignmentStatus(
				companyId,
				purchaseReturnTicket.getIntangibleAsset().getId(),
				com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus.ACTIVE
			)
			.forEach(assignment -> assignment.end(endedAt));
	}

	private void markAssetCollected(PurchaseReturnTicket purchaseReturnTicket) {
		TangibleAsset tangibleAsset = purchaseReturnTicket.getTangibleAsset();
		if (tangibleAsset != null) {
			// 반품 회수된 유형자산은 사내 배정에서 빠지고 사용 가능 상태가 된다.
			tangibleAsset.collectReturn();
			return;
		}

		// 무형자산 반품은 라이선스 자체 반환으로 보고 취소 상태로 전환한다.
		purchaseReturnTicket.getIntangibleAsset().cancel();
	}

	private boolean isAssetAssignable(Ticket ticket, Member member) {
		MemberRole role = member.getRole();
		if (ticketApprovalResolver.requiresAdminAssetApproval(ticket)) {
			return role == MemberRole.ADMIN;
		}
		if (ticketApprovalResolver.requiresAssetManagerApproval(ticket)) {
			return role == MemberRole.ASSET_MANAGER;
		}
		return role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM;
	}

	private boolean isAssetRole(MemberRole role) {
		return role == MemberRole.ADMIN || role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM;
	}
}


