package com.ieumsae.assetieum.domain.ticket.purchasereturn.service;

import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.AssignedAssetValidator;
import com.ieumsae.assetieum.domain.ticket.common.service.IntangibleAssetTicketConflictValidator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketRequesterResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TangibleAssetTicketConflictValidator;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnAvailableAssetResponse;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnAvailableAssetSearchRequest;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.dto.PurchaseReturnTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.entity.PurchaseReturnTicket;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.repository.PurchaseReturnTicketRepository;
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
}
