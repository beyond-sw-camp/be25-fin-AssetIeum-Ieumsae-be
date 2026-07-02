package com.ieumsae.assetieum.domain.ticket.rental.service;

import com.ieumsae.assetieum.global.common.util.KstDateTime;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.log.service.LogService;
import com.ieumsae.assetieum.domain.log.type.AuditLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
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
import com.ieumsae.assetieum.domain.ticket.common.service.AssignedAssetValidator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketRequesterResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TangibleAssetTicketConflictValidator;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.rental.dto.ActiveRentalAssetResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalAssetAssignRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalAssetAssignResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalExtensionDueDateUpdateRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalExtensionDueDateUpdateResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalExtensionTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalExtensionTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalAssignableAssetResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalAssignableAssetSearchRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalAssignableAssetsResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalTicketDetailResponse;
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
	private final TangibleAssetItemRepository tangibleAssetItemRepository;
	private final TangibleAssetRepository tangibleAssetRepository;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TicketRequesterResolver ticketRequesterResolver;
	private final AssignedAssetValidator assignedAssetValidator;
	private final TangibleAssetTicketConflictValidator tangibleAssetTicketConflictValidator;
	private final RentalTicketActionResolver rentalTicketActionResolver;
	private final LogService logService;
	private final NotificationService notificationService;

	public PaginationResponse<AvailableRentalItemResponse> getAvailableRentalItems(
		AuthenticatedMember authenticatedMember,
		AvailableRentalItemSearchRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);

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
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);

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

	public RentalTicketDetailResponse getRentalTicket(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		RentalTicket rentalTicket = findRentalTicket(ticketId, companyId);
		Ticket ticket = rentalTicket.getTicket();

		rentalTicketActionResolver.validateReadable(ticket, viewer);
		boolean requesterView = ticket.getRequester().getId().equals(viewer.getId());

		return RentalTicketDetailResponse.from(
			ticket,
			rentalTicket,
			viewer.getRole(),
			requesterView,
			rentalTicketActionResolver.createActions(ticket, viewer)
		);
	}

	public RentalTicketDetailResponse getRentalExtensionTicket(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		RentalTicket rentalTicket = findRentalTicket(ticketId, companyId);
		Ticket ticket = rentalTicket.getTicket();
		validateRentalExtensionTicket(ticket);

		rentalTicketActionResolver.validateReadable(ticket, viewer);
		boolean requesterView = ticket.getRequester().getId().equals(viewer.getId());

		return RentalTicketDetailResponse.from(
			ticket,
			rentalTicket,
			viewer.getRole(),
			requesterView,
			rentalTicketActionResolver.createActions(ticket, viewer)
		);
	}

	public RentalAssignableAssetsResponse getAssignableAssets(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		RentalAssignableAssetSearchRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		RentalTicket rentalTicket = findRentalTicket(ticketId, companyId);
		Ticket ticket = rentalTicket.getTicket();

		rentalTicketActionResolver.validateReadable(ticket, viewer);

		UUID reservedAssetId = rentalTicket.getTangibleAsset() == null
			? null
			: rentalTicket.getTangibleAsset().getId();
		RentalAssignableAssetResponse reservedAsset = rentalTicket.getTangibleAsset() == null
			? null
			: RentalAssignableAssetResponse.from(rentalTicket.getTangibleAsset(), reservedAssetId);

		Page<RentalAssignableAssetResponse> assets = tangibleAssetRepository.searchRentalAssignableAssets(
				companyId,
				rentalTicket.getTangibleAssetItem().getId(),
				TangibleAssetStatus.AVAILABLE,
				normalize(request.getKeyword()),
				request.toPageable()
			)
			.map(asset -> RentalAssignableAssetResponse.from(asset, reservedAssetId));

		return RentalAssignableAssetsResponse.builder()
			.requestedItem(RentalTicketDetailResponse.ItemSummary.builder()
				.itemId(rentalTicket.getTangibleAssetItem().getId())
				.name(rentalTicket.getTangibleAssetItem().getProductName())
				.manufacturer(rentalTicket.getTangibleAssetItem().getManufacturer())
				.build())
			.reservedAsset(reservedAsset)
			.assets(PaginationResponse.from(assets))
			.build();
	}

	@Transactional
	public RentalAssetAssignResponse assignRentalAsset(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		RentalAssetAssignRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member assignee = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		RentalTicket rentalTicket = findRentalTicket(ticketId, companyId);
		Ticket ticket = rentalTicket.getTicket();

		validateRentalAssignable(ticket, assignee);
		validateRentalAssignee(ticket, assignee);
		validateRentalAssignStatus(ticket);

		TangibleAsset selectedAsset = tangibleAssetRepository.findWithLockByIdAndCompany_Id(
				request.getAssetId(),
				companyId
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));
		validateRentalAssetTarget(rentalTicket, selectedAsset);
		releaseDifferentReservedAssetIfNeeded(rentalTicket, selectedAsset, companyId);

		// 대여 완료의 기준은 실제 자산 배정 이력 생성이다.
		TangibleAssetAssignment assignment = TangibleAssetAssignment.builder()
			.company(ticket.getCompany())
			.tangibleAsset(selectedAsset)
			.member(ticket.getRequester())
			.department(ticket.getDepartment())
			.assignmentType(UsageType.TEMPORARY)
			.assignedAt(rentalTicket.getRentalStartDate())
			.endedAt(rentalTicket.getRequestedDueDate())
			.assignmentStatus(AssignmentStatus.ACTIVE)
			.build();
		tangibleAssetAssignmentRepository.save(assignment);

		selectedAsset.markInUse(
			ticket.getRequester(),
			ticket.getDepartment(),
			UsageType.TEMPORARY,
			resolveAssetUsageType(rentalTicket),
			rentalTicket.getRentalStartDate(),
			rentalTicket.getRequestedDueDate()
		);
		rentalTicket.reserveAsset(selectedAsset);
		rentalTicket.markAssigned();
		// 배정됨 상태를 거친 뒤 공통 티켓과 상세 티켓을 모두 완료 처리한다.
		ticket.changeProcessingStatus(TicketStatus.COMPLETED, KstDateTime.now());
		rentalTicket.complete();
		logService.recordAuditLog(assignee, AuditLogAction.ASSIGN, LogSubjectType.TICKET, ticket.getId(), "대여 자산 배정");
		notifyMember(ticket.getRequester(), "대여 자산이 배정되었습니다.", "요청하신 대여 자산이 배정되었습니다.", ticket);

		return RentalAssetAssignResponse.from(ticket, rentalTicket, selectedAsset);
	}

	@Transactional
	public RentalExtensionDueDateUpdateResponse updateRentalExtensionDueDate(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		RentalExtensionDueDateUpdateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member assignee = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		RentalTicket rentalTicket = findRentalTicket(ticketId, companyId);
		Ticket ticket = rentalTicket.getTicket();
		validateRentalExtensionTicket(ticket);
		validateRentalExtensionUpdateTarget(ticket, assignee);
		validateRentalExtensionDueDate(rentalTicket, request.getReturnDueDate());

		TangibleAsset asset = tangibleAssetRepository.findWithLockByIdAndCompany_Id(
				rentalTicket.getTangibleAsset().getId(),
				companyId
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));
		LocalDateTime previousReturnDueDate = asset.getReturnDueDate();
		TangibleAssetAssignment assignment = tangibleAssetAssignmentRepository
			.findByCompany_IdAndTangibleAsset_IdAndAssignmentStatus(companyId, asset.getId(), AssignmentStatus.ACTIVE)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "활성 대여 배정 이력을 찾을 수 없습니다."));

		// 대여연장 완료의 기준은 자산과 배정 이력의 반납 예정일 확정이다.
		asset.updateReturnDueDate(request.getReturnDueDate());
		assignment.updateEndedAt(request.getReturnDueDate());
		ticket.changeProcessingStatus(TicketStatus.COMPLETED, KstDateTime.now());
		rentalTicket.complete();
		logService.recordAuditLog(assignee, AuditLogAction.INFORMATION_CHANGE, LogSubjectType.TICKET, ticket.getId(), "대여 연장 처리");
		notifyMember(ticket.getRequester(), "대여 연장 처리가 완료되었습니다.", "반납 예정일이 변경되었습니다.", ticket);

		return RentalExtensionDueDateUpdateResponse.from(ticket, rentalTicket, asset, previousReturnDueDate);
	}

	@Transactional
	public RentalTicketCreateResponse createRentalTicket(
		AuthenticatedMember authenticatedMember,
		RentalTicketCreateRequest request
	) {
		validateRentalPeriod(request);

		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
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
		notifyMember(approver, "대여 요청이 접수되었습니다.", "대여 요청을 확인하고 승인 여부를 처리하세요.", ticket);

		return RentalTicketCreateResponse.from(ticket, rentalTicket);
	}

	@Transactional
	public RentalExtensionTicketCreateResponse createRentalExtensionTicket(
		AuthenticatedMember authenticatedMember,
		RentalExtensionTicketCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
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
		notifyMember(approver, "대여 연장 요청이 접수되었습니다.", "대여 연장 요청을 확인하고 승인 여부를 처리하세요.", ticket);

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

		return assignedAssetValidator.isTangibleInUseByAssignee(assignment)
			&& asset.getUsageType() == UsageType.TEMPORARY
			&& asset.getReturnDueDate() != null;
	}

	private void validateRentalAssignable(Ticket ticket, Member member) {
		if (ticketApprovalResolver.requiresAdminAssetApproval(ticket)) {
			if (member.getRole() != MemberRole.ADMIN) {
				throw new BusinessException(ErrorCode.ACCESS_DENIED);
			}
			return;
		}
		if (ticketApprovalResolver.requiresAssetManagerApproval(ticket)) {
			if (member.getRole() != MemberRole.ASSET_MANAGER) {
				throw new BusinessException(ErrorCode.ACCESS_DENIED);
			}
			return;
		}
		if (member.getRole() == MemberRole.ASSET_MANAGER
			|| member.getRole() == MemberRole.ASSET_TEAM) {
			return;
		}
		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}

	private void validateRentalAssignStatus(Ticket ticket) {
		if (ticket.getTicketStatus() != TicketStatus.IN_PROGRESS) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매자산팀 승인 상태의 대여 티켓만 자산을 할당할 수 있습니다.");
		}
	}

	private void validateRentalAssignee(Ticket ticket, Member assignee) {
		if (ticket.getAssignee() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "담당자 지정 후 대여 자산을 할당할 수 있습니다.");
		}
		if (!ticket.getAssignee().getId().equals(assignee.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateRentalExtensionTicket(Ticket ticket) {
		if (ticket.getTicketType() != TicketType.RENTAL_EXTENSION) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "대여연장 티켓이 아닙니다.");
		}
	}

	private void validateRentalExtensionUpdateTarget(Ticket ticket, Member assignee) {
		validateRentalAssignable(ticket, assignee);
		if (ticket.getAssignee() == null || !ticket.getAssignee().getId().equals(assignee.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		if (ticket.getTicketStatus() != TicketStatus.IN_PROGRESS) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "처리중 상태의 대여연장 티켓만 반납 예정일을 변경할 수 있습니다.");
		}
	}

	private void validateRentalExtensionDueDate(RentalTicket rentalTicket, LocalDateTime returnDueDate) {
		if (rentalTicket.getTangibleAsset() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "대여연장 대상 자산을 찾을 수 없습니다.");
		}
		if (!returnDueDate.isAfter(rentalTicket.getRentalDueDate())) {
			throw new BusinessException(ErrorCode.INVALID_RENTAL_PERIOD, "변경 반납 예정일은 기존 반납 예정일보다 이후여야 합니다.");
		}
	}

	private void validateRentalAssetTarget(RentalTicket rentalTicket, TangibleAsset asset) {
		if (!asset.getTangibleAssetItem().getId().equals(rentalTicket.getTangibleAssetItem().getId())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "요청 품목과 다른 대여 자산은 할당할 수 없습니다.");
		}
		if (asset.getTangibleAssetStatus() == TangibleAssetStatus.AVAILABLE) {
			return;
		}
		if (asset.getTangibleAssetStatus() == TangibleAssetStatus.RESERVED
			&& rentalTicket.getTangibleAsset() != null
			&& rentalTicket.getTangibleAsset().getId().equals(asset.getId())) {
			return;
		}
		throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "할당 가능한 대여 자산이 아닙니다.");
	}

	private void releaseDifferentReservedAssetIfNeeded(
		RentalTicket rentalTicket,
		TangibleAsset selectedAsset,
		UUID companyId
	) {
		TangibleAsset reservedAsset = rentalTicket.getTangibleAsset();
		if (reservedAsset == null || reservedAsset.getId().equals(selectedAsset.getId())) {
			return;
		}

		TangibleAsset lockedReservedAsset = tangibleAssetRepository.findWithLockByIdAndCompany_Id(
				reservedAsset.getId(),
				companyId
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));
		if (lockedReservedAsset.getTangibleAssetStatus() == TangibleAssetStatus.RESERVED) {
			lockedReservedAsset.releaseReservation();
		}
	}

	private void validateRentalExtensionTarget(TangibleAssetAssignment assignment, Member requester) {
		TangibleAsset asset = assignment.getTangibleAsset();

		if (!isActiveRentalAsset(assignment)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "연장 요청 가능한 대여중 자산이 아닙니다.");
		}

		assignedAssetValidator.validateTangibleRequester(assignment, requester);
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

	private RentalTicket findRentalTicket(UUID ticketId, UUID companyId) {
		return rentalTicketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
	}

	private AssetUsageType resolveAssetUsageType(RentalTicket rentalTicket) {
		return switch (rentalTicket.getRequestedUsageType()) {
			case PERSONAL -> AssetUsageType.PERSONAL;
			case DEPARTMENT -> AssetUsageType.DEPARTMENT;
		};
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
