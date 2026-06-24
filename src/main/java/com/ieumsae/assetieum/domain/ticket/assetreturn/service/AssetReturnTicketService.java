package com.ieumsae.assetieum.domain.ticket.assetreturn.service;

import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnAvailableAssetResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnAvailableAssetSearchRequest;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnCollectResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnCompleteResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.assetreturn.entity.AssetReturnTicket;
import com.ieumsae.assetieum.domain.ticket.assetreturn.repository.AssetReturnTicketRepository;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.AssignedAssetValidator;
import com.ieumsae.assetieum.domain.ticket.common.service.IntangibleAssetTicketConflictValidator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketRequesterResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TangibleAssetTicketConflictValidator;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
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
public class AssetReturnTicketService {

	private final TicketRepository ticketRepository;
	private final AssetReturnTicketRepository assetReturnTicketRepository;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TicketRequesterResolver ticketRequesterResolver;
	private final AssignedAssetValidator assignedAssetValidator;
	private final TangibleAssetTicketConflictValidator tangibleAssetTicketConflictValidator;
	private final IntangibleAssetTicketConflictValidator intangibleAssetTicketConflictValidator;
	private final NotificationService notificationService;

	public List<AssetReturnAvailableAssetResponse> getAvailableAssets(
		AuthenticatedMember authenticatedMember,
		AssetReturnAvailableAssetSearchRequest request
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
				.map(AssetReturnAvailableAssetResponse::from)
				.toList();
		}

		return intangibleAssetAssignmentRepository.findAllByCompany_IdAndMember_IdAndAssignmentStatus(
				companyId,
				requester.getId(),
				com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus.ACTIVE
			)
			.stream()
			.filter(assignedAssetValidator::isIntangibleInUseByAssignee)
			.map(AssetReturnAvailableAssetResponse::from)
			.toList();
	}

	public AssetReturnTicketDetailResponse getAssetReturnTicket(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		AssetReturnTicket assetReturnTicket = findAssetReturnTicket(ticketId, companyId);
		Ticket ticket = assetReturnTicket.getTicket();

		validateReadable(ticket, viewer);

		return AssetReturnTicketDetailResponse.from(
			ticket,
			assetReturnTicket,
			viewer.getRole(),
			isRequester(ticket, viewer),
			createActions(ticket, assetReturnTicket, viewer)
		);
	}

	@Transactional
	public AssetReturnTicketCreateResponse createAssetReturnTicket(
		AuthenticatedMember authenticatedMember,
		AssetReturnTicketCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);

		if (request.getAssetType() == AssetReturnTargetType.TANGIBLE) {
			return createTangibleReturnTicket(companyId, requester, request);
		}

		return createIntangibleReturnTicket(companyId, requester, request);
	}

	@Transactional
	public AssetReturnCollectResponse collectAssetReturn(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member collector = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		AssetReturnTicket assetReturnTicket = findAssetReturnTicket(ticketId, companyId);

		validateCollectable(ticket, assetReturnTicket, collector);

		LocalDateTime collectedAt = LocalDateTime.now();
		// 회수 시점에 배정 이력을 종료하고, 자산을 실제 회수 완료 상태로 전환한다.
		endActiveAssignment(assetReturnTicket, companyId, collectedAt);
		markAssetCollected(assetReturnTicket);
		assetReturnTicket.collect(collectedAt);
		notifyMember(ticket.getRequester(), "자산 반납이 수거되었습니다.", "자산 반납이 진행됩니다.", ticket);

		return AssetReturnCollectResponse.from(ticket, assetReturnTicket);
	}

	@Transactional
	public AssetReturnCompleteResponse completeAssetReturn(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member processor = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		AssetReturnTicket assetReturnTicket = findAssetReturnTicket(ticketId, companyId);

		validateCompletable(ticket, assetReturnTicket, processor);

		LocalDateTime processedAt = LocalDateTime.now();
		// 완료 처리는 상세 티켓과 공통 티켓을 함께 종료한다.
		completeAssetStatus(assetReturnTicket);
		assetReturnTicket.complete(processedAt);
		ticket.changeProcessingStatus(TicketStatus.COMPLETED, processedAt);
		notifyMember(ticket.getRequester(), "자산 반납이 완료되었습니다.", "자산 반납 처리가 완료되었습니다.", ticket);

		return AssetReturnCompleteResponse.from(ticket, assetReturnTicket);
	}

	private AssetReturnTicketCreateResponse createTangibleReturnTicket(
		UUID companyId,
		Member requester,
		AssetReturnTicketCreateRequest request
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
		AssetReturnTicket assetReturnTicket = assetReturnTicketRepository.save(
			AssetReturnTicket.createTangibleReturn(ticket, requester.getCompany(), assignment.getTangibleAsset())
		);
		// 유형자산은 요청 생성 시 반납 요청 상태로 표시하되, 실제 배정 종료는 회수 시점에 처리한다.
		assignment.getTangibleAsset().requestReturn();
		notifyMember(ticket.getApprover(), "자산 반납 요청이 접수되었습니다.", "자산 반납 요청을 확인하고 승인 여부를 처리하세요.", ticket);

		return AssetReturnTicketCreateResponse.from(ticket, assetReturnTicket);
	}

	private AssetReturnTicketCreateResponse createIntangibleReturnTicket(
		UUID companyId,
		Member requester,
		AssetReturnTicketCreateRequest request
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
		AssetReturnTicket assetReturnTicket = assetReturnTicketRepository.save(
			AssetReturnTicket.createIntangibleReturn(ticket, requester.getCompany(), assignment.getIntangibleAsset())
		);

		return AssetReturnTicketCreateResponse.from(ticket, assetReturnTicket);
	}

	private Ticket createCommonTicket(UUID companyId, Member requester, String requestReason) {
		Member approver = ticketApprovalResolver.resolveDepartmentApprover(requester);

		return ticketRepository.save(Ticket.createAssetReturn(
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
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "반납/해지 요청 가능한 유형자산이 아닙니다.");
		}

		assignedAssetValidator.validateTangibleRequester(assignment, requester);
	}

	private void validateIntangibleReturnTarget(IntangibleAssetAssignment assignment, Member requester) {
		if (!assignedAssetValidator.isIntangibleInUseByAssignee(assignment)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "반납/해지 요청 가능한 무형자산이 아닙니다.");
		}

		assignedAssetValidator.validateIntangibleRequester(assignment, requester);
	}

	private AssetReturnTicket findAssetReturnTicket(UUID ticketId, UUID companyId) {
		return assetReturnTicketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
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

	private AssetReturnTicketDetailResponse.Actions createActions(
		Ticket ticket,
		AssetReturnTicket assetReturnTicket,
		Member viewer
	) {
		if (isRequester(ticket, viewer)) {
			return noActions();
		}

		boolean departmentApprover = ticket.getApprover().getId().equals(viewer.getId());
		boolean requested = ticket.getTicketStatus() == TicketStatus.REQUESTED;
		boolean departmentApproved = ticket.getTicketStatus() == TicketStatus.DEPARTMENT_APPROVED;
		boolean assetAssignable = isAssetAssignable(ticket, viewer);
		boolean assignee = ticket.getAssignee() != null && ticket.getAssignee().getId().equals(viewer.getId());

		return AssetReturnTicketDetailResponse.Actions.builder()
			.canApproveDepartment(departmentApprover && requested)
			.canRejectDepartment(departmentApprover && requested)
			.canAssignAsset(assetAssignable && ticket.getAssignee() == null && (requested || departmentApproved))
			.canApproveAsset(assetAssignable && departmentApproved && assignee)
			.canRejectAsset(assetAssignable && departmentApproved && assignee)
			.canCollectAsset(canCollectAsset(ticket, assetReturnTicket, viewer))
			.canCompleteReturn(canCompleteReturn(ticket, assetReturnTicket, viewer))
			.build();
	}

	private AssetReturnTicketDetailResponse.Actions noActions() {
		return AssetReturnTicketDetailResponse.Actions.builder()
			.canApproveDepartment(false)
			.canRejectDepartment(false)
			.canAssignAsset(false)
			.canApproveAsset(false)
			.canRejectAsset(false)
			.canCollectAsset(false)
			.canCompleteReturn(false)
			.build();
	}

	private boolean canCollectAsset(Ticket ticket, AssetReturnTicket assetReturnTicket, Member viewer) {
		return isAssetRole(viewer.getRole())
			&& ticket.getTicketStatus() == TicketStatus.IN_PROGRESS
			&& assetReturnTicket.getStatus() == AssetReturnTicketStatus.REQUESTED
			&& ticket.getAssignee() != null
			&& ticket.getAssignee().getId().equals(viewer.getId());
	}

	private boolean canCompleteReturn(Ticket ticket, AssetReturnTicket assetReturnTicket, Member viewer) {
		return isAssetRole(viewer.getRole())
			&& ticket.getTicketStatus() == TicketStatus.IN_PROGRESS
			&& assetReturnTicket.getStatus() == AssetReturnTicketStatus.COLLECTED
			&& ticket.getAssignee() != null
			&& ticket.getAssignee().getId().equals(viewer.getId());
	}

	private void validateCollectable(Ticket ticket, AssetReturnTicket assetReturnTicket, Member collector) {
		if (!canCollectAsset(ticket, assetReturnTicket, collector)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회수 가능한 반납/해지 티켓이 아닙니다.");
		}
	}

	private void validateCompletable(Ticket ticket, AssetReturnTicket assetReturnTicket, Member processor) {
		if (!canCompleteReturn(ticket, assetReturnTicket, processor)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "완료 처리 가능한 반납/해지 티켓이 아닙니다.");
		}
	}

	private void endActiveAssignment(AssetReturnTicket assetReturnTicket, UUID companyId, LocalDateTime endedAt) {
		if (assetReturnTicket.getTangibleAsset() != null) {
			tangibleAssetAssignmentRepository.findByCompany_IdAndTangibleAsset_IdAndAssignmentStatus(
					companyId,
					assetReturnTicket.getTangibleAsset().getId(),
					AssignmentStatus.ACTIVE
				)
				.ifPresent(assignment -> assignment.end(endedAt));
			return;
		}

		intangibleAssetAssignmentRepository.findAllByCompany_IdAndIntangibleAsset_IdAndAssignmentStatus(
				companyId,
				assetReturnTicket.getIntangibleAsset().getId(),
				com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus.ACTIVE
			)
			.forEach(assignment -> assignment.end(endedAt));
	}

	private void markAssetCollected(AssetReturnTicket assetReturnTicket) {
		TangibleAsset tangibleAsset = assetReturnTicket.getTangibleAsset();
		if (tangibleAsset != null) {
			// 유형자산 반납은 재사용 가능한 재고로 돌아간다.
			tangibleAsset.collectReturn();
			return;
		}

		// 무형자산 해지는 라이선스 사용 종료로 보고 취소 상태로 전환한다.
		assetReturnTicket.getIntangibleAsset().cancel();
	}

	private void completeAssetStatus(AssetReturnTicket assetReturnTicket) {
		TangibleAsset tangibleAsset = assetReturnTicket.getTangibleAsset();
		if (tangibleAsset != null) {
			tangibleAsset.completeReturn();
			return;
		}

		assetReturnTicket.getIntangibleAsset().cancel();
	}

	private boolean isRequester(Ticket ticket, Member viewer) {
		return ticket.getRequester().getId().equals(viewer.getId());
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
}
