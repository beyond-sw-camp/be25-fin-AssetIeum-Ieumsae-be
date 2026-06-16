package com.ieumsae.assetieum.domain.ticket.purchasereturn.service;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.ticket.assetreturn.repository.AssetReturnTicketRepository;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TangibleAssetTicketConflictValidator;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
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

	private static final List<TicketStatus> ONGOING_TICKET_STATUSES = List.of(
		TicketStatus.REQUESTED,
		TicketStatus.DEPARTMENT_APPROVED,
		TicketStatus.IN_PROGRESS
	);

	private final TicketRepository ticketRepository;
	private final PurchaseReturnTicketRepository purchaseReturnTicketRepository;
	private final AssetReturnTicketRepository assetReturnTicketRepository;
	private final MemberRepository memberRepository;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TangibleAssetTicketConflictValidator tangibleAssetTicketConflictValidator;

	public List<PurchaseReturnAvailableAssetResponse> getAvailableAssets(
		AuthenticatedMember authenticatedMember,
		PurchaseReturnAvailableAssetSearchRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = findActiveRequester(authenticatedMember.id(), companyId);

		if (request.getAssetType() == AssetReturnTargetType.TANGIBLE) {
			return tangibleAssetAssignmentRepository.findAllByCompany_IdAndMember_IdAndAssignmentStatus(
					companyId,
					requester.getId(),
					AssignmentStatus.ACTIVE
				)
				.stream()
				.filter(this::isReturnAvailableTangibleAsset)
				.map(PurchaseReturnAvailableAssetResponse::from)
				.toList();
		}

		return intangibleAssetAssignmentRepository.findAllByCompany_IdAndMember_IdAndAssignmentStatus(
				companyId,
				requester.getId(),
				com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus.ACTIVE
			)
			.stream()
			.filter(this::isReturnAvailableIntangibleAsset)
			.map(PurchaseReturnAvailableAssetResponse::from)
			.toList();
	}

	@Transactional
	public PurchaseReturnTicketCreateResponse createPurchaseReturnTicket(
		AuthenticatedMember authenticatedMember,
		PurchaseReturnTicketCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = findActiveRequester(authenticatedMember.id(), companyId);

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
		validateNoOngoingIntangibleReturn(companyId, assignment.getIntangibleAsset().getId());

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

	private Member findActiveRequester(UUID memberId, UUID companyId) {
		Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		return member;
	}

	private boolean isReturnAvailableTangibleAsset(TangibleAssetAssignment assignment) {
		TangibleAsset asset = assignment.getTangibleAsset();

		return asset.getTangibleAssetStatus() == TangibleAssetStatus.IN_USE
			&& asset.getMember() != null
			&& asset.getMember().getId().equals(assignment.getMember().getId())
			&& asset.getCompany().getId().equals(assignment.getCompany().getId());
	}

	private boolean isReturnAvailableIntangibleAsset(IntangibleAssetAssignment assignment) {
		IntangibleAsset asset = assignment.getIntangibleAsset();

		return asset.getIntangibleAssetStatus() == IntangibleAssetStatus.IN_USE
			&& asset.getMember() != null
			&& asset.getMember().getId().equals(assignment.getMember().getId())
			&& asset.getCompany().getId().equals(assignment.getCompany().getId());
	}

	private void validateTangibleReturnTarget(TangibleAssetAssignment assignment, Member requester) {
		if (!isReturnAvailableTangibleAsset(assignment)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "반품 요청 가능한 유형자산이 아닙니다.");
		}

		if (!assignment.getMember().getId().equals(requester.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateIntangibleReturnTarget(IntangibleAssetAssignment assignment, Member requester) {
		if (!isReturnAvailableIntangibleAsset(assignment)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "반품 요청 가능한 무형자산이 아닙니다.");
		}

		if (!assignment.getMember().getId().equals(requester.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateNoOngoingIntangibleReturn(UUID companyId, UUID assetId) {
		boolean existsAssetReturn = assetReturnTicketRepository
			.existsByCompany_IdAndIntangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
				companyId,
				assetId,
				ONGOING_TICKET_STATUSES
			);
		boolean existsPurchaseReturn = purchaseReturnTicketRepository
			.existsByCompany_IdAndIntangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
				companyId,
				assetId,
				ONGOING_TICKET_STATUSES
			);

		if (existsAssetReturn || existsPurchaseReturn) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 진행 중인 무형자산 반납/반품 요청이 있습니다.");
		}
	}
}
