package com.ieumsae.assetieum.domain.ticket.rental.service;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.AssetUsageType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.AvailableRentalItemResponse;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.rental.dto.AvailableRentalItemSearchRequest;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TangibleAssetTicketConflictValidator;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.rental.dto.ActiveRentalAssetResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalExtensionTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalExtensionTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.rental.entity.RentalTicket;
import com.ieumsae.assetieum.domain.ticket.rental.repository.RentalTicketRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentalTicketService {

	private final TicketRepository ticketRepository;
	private final RentalTicketRepository rentalTicketRepository;
	private final MemberRepository memberRepository;
	private final TangibleAssetItemRepository tangibleAssetItemRepository;
	private final TangibleAssetRepository tangibleAssetRepository;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TangibleAssetTicketConflictValidator tangibleAssetTicketConflictValidator;

	public PaginationResponse<AvailableRentalItemResponse> getAvailableRentalItems(
		AuthenticatedMember authenticatedMember,
		AvailableRentalItemSearchRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		findActiveRequester(authenticatedMember.id(), companyId);

		Page<AvailableRentalItemResponse> responsePage = tangibleAssetItemRepository.searchAvailableRentalItems(
			companyId,
			request.getCategoryId(),
			request.getKeyword(),
			request.getIsStandard(),
			request.toPageable()
		);

		return PaginationResponse.from(responsePage);
	}

	public List<ActiveRentalAssetResponse> getActiveRentalAssets(
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
			.filter(this::isActiveRentalAsset)
			.map(ActiveRentalAssetResponse::from)
			.toList();
	}

	@Transactional
	public RentalTicketCreateResponse createRentalTicket(
		AuthenticatedMember authenticatedMember,
		RentalTicketCreateRequest request
	) {
		validateRentalPeriod(request);

		UUID companyId = authenticatedMember.companyId();
		Member requester = findActiveRequester(authenticatedMember.id(), companyId);
		Member approver = ticketApprovalResolver.resolveDepartmentApprover(requester);
		TangibleAssetItem item = findTangibleAssetItem(request.getTangibleAssetItemId(), companyId);
		validateAvailableRentalItem(companyId, item.getId());

		Ticket ticket = ticketRepository.save(Ticket.createRental(
			requester.getCompany(),
			ticketNoGenerator.generate(companyId),
			requester,
			requester.getDepartment(),
			approver,
			normalize(request.getRequestReason())
		));

		RentalTicket rentalTicket = rentalTicketRepository.save(RentalTicket.createRequest(
			ticket,
			requester.getCompany(),
			request.getRequestedUsageType(),
			item,
			request.getRentalStartDate(),
			request.getRequestedDueDate()
		));

		return RentalTicketCreateResponse.from(ticket, rentalTicket);
	}

	@Transactional
	public RentalExtensionTicketCreateResponse createRentalExtensionTicket(
		AuthenticatedMember authenticatedMember,
		RentalExtensionTicketCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = findActiveRequester(authenticatedMember.id(), companyId);
		TangibleAssetAssignment assignment = findActiveAssignment(
			request.getAssignmentId(),
			companyId,
			requester.getId()
		);
		validateRentalExtensionTarget(assignment, requester);
		validateRentalExtensionPeriod(assignment.getTangibleAsset(), request.getRequestedDueDate());
		tangibleAssetTicketConflictValidator.validateNoOngoingTangibleAssetTicket(
			companyId,
			assignment.getTangibleAsset().getId()
		);

		Member approver = ticketApprovalResolver.resolveDepartmentApprover(requester);
		TangibleAsset asset = assignment.getTangibleAsset();

		Ticket ticket = ticketRepository.save(Ticket.createRentalExtension(
			requester.getCompany(),
			ticketNoGenerator.generate(companyId),
			requester,
			requester.getDepartment(),
			approver,
			normalize(request.getRequestReason())
		));

		RentalTicket rentalTicket = rentalTicketRepository.save(RentalTicket.createExtensionRequest(
			ticket,
			requester.getCompany(),
			resolveRequestedUsageType(asset),
			asset,
			asset.getTangibleAssetItem(),
			resolveRentalStartDate(asset, assignment),
			asset.getReturnDueDate(),
			request.getRequestedDueDate()
		));

		return RentalExtensionTicketCreateResponse.from(ticket, rentalTicket, assignment);
	}

	private void validateRentalPeriod(RentalTicketCreateRequest request) {
		if (!request.getRentalStartDate().isBefore(request.getRequestedDueDate())) {
			throw new BusinessException(
				ErrorCode.INVALID_RENTAL_PERIOD,
				"반납 예정 일시는 대여 시작 일시보다 이후여야 합니다."
			);
		}
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
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "대여중인 자산 배정을 찾을 수 없습니다."));
	}

	private boolean isActiveRentalAsset(TangibleAssetAssignment assignment) {
		TangibleAsset asset = assignment.getTangibleAsset();

		return asset.getTangibleAssetStatus() == TangibleAssetStatus.IN_USE
			&& asset.getUsageType() == UsageType.TEMPORARY
			&& asset.getReturnDueDate() != null
			&& asset.getMember() != null
			&& asset.getMember().getId().equals(assignment.getMember().getId())
			&& asset.getCompany().getId().equals(assignment.getCompany().getId());
	}

	private void validateRentalExtensionTarget(TangibleAssetAssignment assignment, Member requester) {
		TangibleAsset asset = assignment.getTangibleAsset();

		if (!isActiveRentalAsset(assignment)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "연장 요청 가능한 대여중 자산이 아닙니다.");
		}

		if (asset.getMember() == null || !asset.getMember().getId().equals(requester.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateRentalExtensionPeriod(TangibleAsset asset, LocalDateTime requestedDueDate) {
		if (!requestedDueDate.isAfter(asset.getReturnDueDate())) {
			throw new BusinessException(ErrorCode.INVALID_RENTAL_PERIOD, "연장 요청 반납 예정 일시는 현재 반납 예정 일시보다 이후여야 합니다.");
		}
	}

	private RequestedUsageType resolveRequestedUsageType(TangibleAsset asset) {
		if (asset.getAssetUsageType() == AssetUsageType.DEPARTMENT) {
			return RequestedUsageType.DEPARTMENT;
		}

		return RequestedUsageType.PERSONAL;
	}

	private LocalDateTime resolveRentalStartDate(TangibleAsset asset, TangibleAssetAssignment assignment) {
		if (asset.getUsedStartedAt() != null) {
			return asset.getUsedStartedAt();
		}

		return assignment.getAssignedAt();
	}

	private TangibleAssetItem findTangibleAssetItem(UUID itemId, UUID companyId) {
		TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

		if (!item.getCompany().getId().equals(companyId)) {
			throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND);
		}
		return item;
	}

	private void validateAvailableRentalItem(UUID companyId, UUID itemId) {
		boolean exists = tangibleAssetRepository.existsByCompany_IdAndTangibleAssetItem_IdAndTangibleAssetStatus(
			companyId,
			itemId,
			TangibleAssetStatus.AVAILABLE
		);

		if (!exists) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "대여 가능한 자산이 없는 품목입니다.");
		}
	}

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
