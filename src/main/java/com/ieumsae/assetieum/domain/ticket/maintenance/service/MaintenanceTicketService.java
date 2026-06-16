package com.ieumsae.assetieum.domain.ticket.maintenance.service;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TangibleAssetTicketConflictValidator;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceAvailableAssetResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.maintenance.dto.MaintenanceTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.maintenance.entity.MaintenanceTicket;
import com.ieumsae.assetieum.domain.ticket.maintenance.repository.MaintenanceTicketRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
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
	private final MemberRepository memberRepository;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TangibleAssetTicketConflictValidator tangibleAssetTicketConflictValidator;

	public List<MaintenanceAvailableAssetResponse> getAvailableAssets(
		AuthenticatedMember authenticatedMember
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = findActiveRequester(authenticatedMember.id(), companyId);

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
		Member requester = findActiveRequester(authenticatedMember.id(), companyId);
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

		return MaintenanceTicketCreateResponse.from(ticket, maintenanceTicket, assignment);
	}

	private Member findActiveRequester(UUID memberId, UUID companyId) {
		Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		return member;
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
		TangibleAsset asset = assignment.getTangibleAsset();

		return asset.getTangibleAssetStatus() == TangibleAssetStatus.IN_USE
			&& asset.getMember() != null
			&& asset.getMember().getId().equals(assignment.getMember().getId())
			&& asset.getCompany().getId().equals(assignment.getCompany().getId());
	}

	private void validateMaintenanceTarget(TangibleAssetAssignment assignment, Member requester) {
		TangibleAsset asset = assignment.getTangibleAsset();

		if (!isMaintenanceAvailableAsset(assignment)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유지보수 요청 가능한 사용 중 자산이 아닙니다.");
		}

		if (asset.getMember() == null || !asset.getMember().getId().equals(requester.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

}
