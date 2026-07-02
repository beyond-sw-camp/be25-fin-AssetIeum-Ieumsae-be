package com.ieumsae.assetieum.domain.ticket.maintenance.service;

import com.ieumsae.assetieum.global.common.util.KstDateTime;

import com.ieumsae.assetieum.domain.budget.budget.service.BudgetExecutionService;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.log.service.LogService;
import com.ieumsae.assetieum.domain.log.type.AuditLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.AssignedAssetValidator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketRequesterResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TangibleAssetTicketConflictValidator;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceAvailableAssetResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceAssetCollectResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCompleteRequest;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCompleteResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.entity.MaintenanceTicket;
import com.ieumsae.assetieum.domain.ticket.maintenance.repository.MaintenanceTicketRepository;
import com.ieumsae.assetieum.domain.ticket.maintenance.type.MaintenanceTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceTicketService {

	private final TicketRepository ticketRepository;
	private final MaintenanceTicketRepository maintenanceTicketRepository;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TicketRequesterResolver ticketRequesterResolver;
	private final AssignedAssetValidator assignedAssetValidator;
	private final TangibleAssetTicketConflictValidator tangibleAssetTicketConflictValidator;
	private final BudgetExecutionService budgetExecutionService;
	private final LogService logService;
	private final NotificationService notificationService;

	public List<MaintenanceAvailableAssetResponse> getAvailableAssets(
		AuthenticatedMember authenticatedMember
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);

		return tangibleAssetAssignmentRepository.findAllByCompany_IdAndMember_IdAndAssignmentStatus(
				companyId,
				requester.getId(),
				AssignmentStatus.ACTIVE
			)
			.stream()
			.filter(this::isMaintenanceAvailableAsset)
			.map(MaintenanceAvailableAssetResponse::from)
			.toList();
	}

	@Transactional
	public MaintenanceTicketCreateResponse createMaintenanceTicket(
		AuthenticatedMember authenticatedMember,
		MaintenanceTicketCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		TangibleAssetAssignment assignment = findActiveAssignment(
			request.getAssignmentId(),
			companyId,
			requester.getId()
		);
		validateMaintenanceTarget(assignment, requester);
		tangibleAssetTicketConflictValidator.validateNoOngoingTangibleAssetTicket(
			companyId,
			assignment.getTangibleAsset().getId()
		);

		TangibleAsset asset = assignment.getTangibleAsset();
		Member approver = ticketApprovalResolver.resolveDepartmentApprover(requester);

		Ticket ticket = ticketRepository.save(Ticket.createMaintenanceRequest(
			requester.getCompany(),
			ticketNoGenerator.generate(companyId),
			requester,
			requester.getDepartment(),
			approver,
			request.getRequestDetail().trim()
		));

		MaintenanceTicket maintenanceTicket = maintenanceTicketRepository.save(MaintenanceTicket.createRequest(
			ticket,
			requester.getCompany(),
			asset
		));
		asset.requestRepair();
		notifyMember(approver, "수선 요청이 접수되었습니다.", "수선 요청을 확인하고 승인 여부를 처리하세요.", ticket);

		return MaintenanceTicketCreateResponse.from(ticket, maintenanceTicket, assignment);
	}

	public MaintenanceTicketDetailResponse getMaintenanceTicket(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		MaintenanceTicket maintenanceTicket = findMaintenanceTicket(ticketId, companyId);
		Ticket ticket = maintenanceTicket.getTicket();

		validateReadable(ticket, viewer);

		return MaintenanceTicketDetailResponse.from(
			ticket,
			maintenanceTicket,
			viewer.getRole(),
			isRequester(ticket, viewer),
			createActions(ticket, maintenanceTicket, viewer)
		);
	}

	@Transactional
	public MaintenanceAssetCollectResponse collectMaintenanceAsset(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member collector = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		MaintenanceTicket maintenanceTicket = findMaintenanceTicket(ticketId, companyId);

		validateCollectable(ticket, maintenanceTicket, collector);
		// 회수 버튼을 누르면 실제 자산 상태를 수리중으로 전환한다.
		maintenanceTicket.getTangibleAsset().startRepair();
		maintenanceTicket.collect(KstDateTime.now());
		logService.recordAuditLog(collector, AuditLogAction.INFORMATION_CHANGE, LogSubjectType.TICKET, ticket.getId(), "유지보수 수거");
		notifyMember(ticket.getRequester(), "수선 자산이 수거되었습니다.", "수선이 진행됩니다.", ticket);

		return MaintenanceAssetCollectResponse.from(ticket, maintenanceTicket);
	}

	@Transactional
	public MaintenanceTicketCompleteResponse completeMaintenanceTicket(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		MaintenanceTicketCompleteRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member processor = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		MaintenanceTicket maintenanceTicket = findMaintenanceTicket(ticketId, companyId);

		validateCompletable(ticket, maintenanceTicket, processor);

		BigDecimal maintenanceCost = normalizeMaintenanceCost(request.getMaintenanceCost());
		LocalDateTime completedAt = KstDateTime.now();
		// 유지보수 비용이 있으면 완료 시점에 공통 예산을 사용 처리한다.
		budgetExecutionService.executeForMaintenanceCompletion(ticket, companyId, maintenanceCost);
		// 수리 완료 후 자산은 기존 사용자에게 다시 사용중 상태로 복구된다.
		maintenanceTicket.getTangibleAsset().completeRepair();
		maintenanceTicket.complete(normalizeMaintenanceResult(request.getMaintenanceResult()), maintenanceCost, completedAt);
		ticket.changeProcessingStatus(TicketStatus.COMPLETED, completedAt);
		logService.recordAuditLog(processor, AuditLogAction.INFORMATION_CHANGE, LogSubjectType.TICKET, ticket.getId(), "유지보수 완료");
		notifyMember(ticket.getRequester(), "수선이 완료되었습니다.", "수선 처리가 완료되었습니다.", ticket);

		return MaintenanceTicketCompleteResponse.from(
			ticket,
			maintenanceTicket,
			maintenanceCost.signum() > 0
		);
	}

	private TangibleAssetAssignment findActiveAssignment(UUID assignmentId, UUID companyId, UUID memberId) {
		return tangibleAssetAssignmentRepository.findByIdAndCompany_IdAndMember_IdAndAssignmentStatus(
				assignmentId,
				companyId,
				memberId,
				AssignmentStatus.ACTIVE
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용 중인 자산 배정을 찾을 수 없습니다."));
	}

	private boolean isMaintenanceAvailableAsset(TangibleAssetAssignment assignment) {
		return assignedAssetValidator.isTangibleInUseByAssignee(assignment);
	}

	private void validateMaintenanceTarget(TangibleAssetAssignment assignment, Member requester) {
		TangibleAsset asset = assignment.getTangibleAsset();

		if (!isMaintenanceAvailableAsset(assignment)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유지보수 요청 가능한 사용 중 자산이 아닙니다.");
		}

		assignedAssetValidator.validateTangibleRequester(assignment, requester);
	}

	private MaintenanceTicket findMaintenanceTicket(UUID ticketId, UUID companyId) {
		return maintenanceTicketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
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

	private MaintenanceTicketDetailResponse.Actions createActions(
		Ticket ticket,
		MaintenanceTicket maintenanceTicket,
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
		return MaintenanceTicketDetailResponse.Actions.builder()
			.canApproveDepartment(departmentApprover && requested)
			.canRejectDepartment(departmentApprover && requested)
			.canAssignAsset(assetAssignable && ticket.getAssignee() == null && (requested || departmentApproved))
			.canApproveAsset(assetAssignable && departmentApproved && assignee)
			.canRejectAsset(assetAssignable && departmentApproved && assignee)
			.canChangeProcessingStatus(false)
			.canCollectAsset(canCollectAsset(ticket, maintenanceTicket, viewer))
			.canCompleteMaintenance(canCompleteMaintenance(ticket, maintenanceTicket, viewer))
			.build();
	}

	private MaintenanceTicketDetailResponse.Actions noActions() {
		return MaintenanceTicketDetailResponse.Actions.builder()
			.canApproveDepartment(false)
			.canRejectDepartment(false)
			.canAssignAsset(false)
			.canApproveAsset(false)
			.canRejectAsset(false)
			.canChangeProcessingStatus(false)
			.canCollectAsset(false)
			.canCompleteMaintenance(false)
			.build();
	}

	private boolean canCollectAsset(Ticket ticket, MaintenanceTicket maintenanceTicket, Member viewer) {
		return isAssetRole(viewer.getRole())
			&& ticket.getTicketStatus() == TicketStatus.IN_PROGRESS
			&& maintenanceTicket.getStatus() == MaintenanceTicketStatus.REQUESTED
			&& ticket.getAssignee() != null
			&& ticket.getAssignee().getId().equals(viewer.getId());
	}

	private boolean canCompleteMaintenance(Ticket ticket, MaintenanceTicket maintenanceTicket, Member viewer) {
		return isAssetRole(viewer.getRole())
			&& ticket.getTicketStatus() == TicketStatus.IN_PROGRESS
			&& maintenanceTicket.getStatus() == MaintenanceTicketStatus.COLLECTED
			&& ticket.getAssignee() != null
			&& ticket.getAssignee().getId().equals(viewer.getId());
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

	private void validateCollectable(Ticket ticket, MaintenanceTicket maintenanceTicket, Member collector) {
		if (!canCollectAsset(ticket, maintenanceTicket, collector)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회수 가능한 유지보수 티켓이 아닙니다.");
		}
	}

	private boolean isAssetRole(MemberRole role) {
		return role == MemberRole.ADMIN || role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM;
	}

	private void validateCompletable(Ticket ticket, MaintenanceTicket maintenanceTicket, Member processor) {
		if (!canCompleteMaintenance(ticket, maintenanceTicket, processor)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "완료 처리 가능한 유지보수 티켓이 아닙니다.");
		}
	}

	private BigDecimal normalizeMaintenanceCost(BigDecimal maintenanceCost) {
		if (maintenanceCost == null) {
			return BigDecimal.ZERO;
		}
		return maintenanceCost.max(BigDecimal.ZERO);
	}

	private String normalizeMaintenanceResult(String maintenanceResult) {
		if (maintenanceResult == null || maintenanceResult.isBlank()) {
			return null;
		}
		return maintenanceResult.trim();
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
