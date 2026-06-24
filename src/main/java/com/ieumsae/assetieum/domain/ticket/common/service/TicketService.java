package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.domain.budget.budget.service.BudgetExecutionService;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchasePlanItemStatus;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.repository.PurchasePlanItemRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.AssetUsageType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.assetrequest.repository.AssetRequestTicketRepository;
import com.ieumsae.assetieum.domain.ticket.assetreturn.entity.AssetReturnTicket;
import com.ieumsae.assetieum.domain.ticket.assetreturn.repository.AssetReturnTicketRepository;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.dto.AssetApprovalResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.DepartmentApprovalResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketAssigneeResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketCancelResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketListItemResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketProcessingStatusUpdateRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketProcessingStatusUpdateResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketRejectionRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketSearchRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketStatisticsResponse;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.maintenance.entity.MaintenanceTicket;
import com.ieumsae.assetieum.domain.ticket.maintenance.repository.MaintenanceTicketRepository;
import com.ieumsae.assetieum.domain.ticket.maintenance.type.MaintenanceTicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.DirectPurchaseResult;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.DirectPurchaseResultRepository;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.PurchaseRequestTicketRepository;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.ConfirmationStatus;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.entity.PurchaseReturnTicket;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.repository.PurchaseReturnTicketRepository;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.type.PurchaseReturnTicketStatus;
import com.ieumsae.assetieum.domain.ticket.rental.entity.RentalTicket;
import com.ieumsae.assetieum.domain.ticket.rental.repository.RentalTicketRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

	private final TicketRepository ticketRepository;
	private final MemberRepository memberRepository;
	private final DepartmentRepository departmentRepository;
	private final RentalTicketRepository rentalTicketRepository;
	private final TangibleAssetRepository tangibleAssetRepository;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final AssetRequestTicketRepository assetRequestTicketRepository;
	private final PurchaseRequestTicketRepository purchaseRequestTicketRepository;
	private final PurchaseReturnTicketRepository purchaseReturnTicketRepository;
	private final DirectPurchaseResultRepository directPurchaseResultRepository;
	private final PurchasePlanItemRepository purchasePlanItemRepository;
	private final MaintenanceTicketRepository maintenanceTicketRepository;
	private final AssetReturnTicketRepository assetReturnTicketRepository;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final BudgetExecutionService budgetExecutionService;
	private final NotificationService notificationService;

	@Transactional
	public TicketAssigneeResponse assignMe(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member assignee = findActiveMember(authenticatedMember.id(), companyId);
		Ticket ticket = findActiveTicket(ticketId, companyId);
		validateAssetAssignable(ticket, assignee);
		validateAssignableStatus(ticket);
		validateUnassigned(ticket);

		ticket.assign(assignee);
		notifyTicketRequester(ticket, "티켓 담당자가 지정되었습니다.", "자산 팀이 요청을 처리할 수 있습니다.");

		return TicketAssigneeResponse.from(ticket);
	}

	@Transactional
	public TicketCancelResponse cancelTicket(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member member = findActiveMember(authenticatedMember.id(), companyId);
		Ticket ticket = findActiveTicket(ticketId, companyId);
		validateCancellable(ticket, member);
		validateDirectPurchaseResultNotRegisteredForCancel(ticket, companyId);
		validatePurchasePlanNotLinkedForCancel(ticket, companyId);

		releaseReservedRentalAssetIfNeeded(ticket, companyId);
		budgetExecutionService.releaseHoldForCancellation(ticket, companyId);
		ticket.cancel(LocalDateTime.now());
		syncCancelledDetailStatusIfNeeded(ticket, companyId);
		syncCancelledPurchaseRequestStatusIfNeeded(ticket, companyId);
		syncCancelledRentalStatusIfNeeded(ticket, companyId);
		syncCancelledMaintenanceStatusIfNeeded(ticket, companyId);
		syncCancelledAssetReturnStatusIfNeeded(ticket, companyId);
		syncCancelledPurchaseReturnStatusIfNeeded(ticket, companyId);
		notifyTicketRequester(ticket, "티켓이 취소되었습니다.", "취소된 요청을 확인하세요.");

		return TicketCancelResponse.from(ticket);
	}

	@Transactional
	public TicketCancelResponse cancelTicketForOffboarding(UUID companyId, UUID ticketId) {
		Ticket ticket = findActiveTicket(ticketId, companyId);
		validateDirectPurchaseResultNotRegisteredForCancel(ticket, companyId);
		validatePurchasePlanNotLinkedForCancel(ticket, companyId);

		releaseReservedRentalAssetIfNeeded(ticket, companyId);
		budgetExecutionService.releaseHoldForCancellation(ticket, companyId);
		ticket.cancel(LocalDateTime.now());
		syncCancelledDetailStatusIfNeeded(ticket, companyId);
		syncCancelledPurchaseRequestStatusIfNeeded(ticket, companyId);
		syncCancelledRentalStatusIfNeeded(ticket, companyId);
		syncCancelledMaintenanceStatusIfNeeded(ticket, companyId);
		syncCancelledAssetReturnStatusIfNeeded(ticket, companyId);
		syncCancelledPurchaseReturnStatusIfNeeded(ticket, companyId);
		notifyTicketRequester(ticket, "티켓이 취소되었습니다.", "취소된 요청을 확인하세요.");

		return TicketCancelResponse.from(ticket);
	}

	@Transactional
	public DepartmentApprovalResponse approveDepartment(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member approver = findActiveMember(authenticatedMember.id(), companyId);
		Ticket ticket = findActiveTicket(ticketId, companyId);
		validateDepartmentApprover(ticket, approver);
		validateTicketStatus(ticket, TicketStatus.REQUESTED, "요청 상태의 티켓만 부서장 승인 처리할 수 있습니다.");

		// 대여 티켓은 부서장 승인 시 가용 자산 1개를 선점해 중복 대여를 막는다.
		reserveRentalAssetIfNeeded(ticket, companyId);
		budgetExecutionService.holdForAssetRequest(ticket, companyId);
		budgetExecutionService.holdForPurchaseRequest(ticket, companyId);
		ticket.approveDepartment(LocalDateTime.now());
		notifyTicketRequester(ticket, "티켓이 부서 승인되었습니다.", "자산 승인 처리를 진행하세요.");

		return DepartmentApprovalResponse.from(ticket);
	}

	@Transactional
	public void approveDepartmentForHrEvent(
		UUID companyId,
		UUID ticketId
	) {
		Ticket ticket = findActiveTicket(ticketId, companyId);
		validateTicketStatus(ticket, TicketStatus.REQUESTED, "HR 이벤트로 생성된 요청 상태의 티켓만 부서장 승인 처리할 수 있습니다.");

		reserveRentalAssetIfNeeded(ticket, companyId);
		budgetExecutionService.holdForAssetRequest(ticket, companyId);
		budgetExecutionService.holdForPurchaseRequest(ticket, companyId);
		ticket.approveDepartment(LocalDateTime.now());
		notifyTicketRequester(ticket, "티켓이 부서 승인되었습니다.", "자산 승인 처리를 진행하세요.");

	}

	@Transactional
	public DepartmentApprovalResponse rejectDepartment(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		TicketRejectionRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member approver = findActiveMember(authenticatedMember.id(), companyId);
		Ticket ticket = findActiveTicket(ticketId, companyId);
		validateDepartmentApprover(ticket, approver);
		validateTicketStatus(ticket, TicketStatus.REQUESTED, "요청 상태의 티켓만 부서장 반려 처리할 수 있습니다.");

		ticket.rejectDepartment(request.getRejectionReason().trim(), LocalDateTime.now());
		syncCancelledDetailStatusIfNeeded(ticket, companyId);
		syncCancelledPurchaseRequestStatusIfNeeded(ticket, companyId);
		syncCancelledRentalStatusIfNeeded(ticket, companyId);
		syncRejectedMaintenanceStatusIfNeeded(ticket, companyId);
		syncRejectedAssetReturnStatusIfNeeded(ticket, companyId);
		syncRejectedPurchaseReturnStatusIfNeeded(ticket, companyId);
		notifyTicketRequester(ticket, "티켓이 부서 반려되었습니다.", "반려 사유를 확인하세요.");

		return DepartmentApprovalResponse.from(ticket);
	}

	@Transactional
	public AssetApprovalResponse approveAsset(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member assignee = findActiveMember(authenticatedMember.id(), companyId);
		Ticket ticket = findActiveTicket(ticketId, companyId);
		validateAssetAssignable(ticket, assignee);
		validateTicketStatus(ticket, TicketStatus.DEPARTMENT_APPROVED, "부서장 승인된 티켓만 구매자산팀 승인 처리할 수 있습니다.");
		validateAssignee(ticket, assignee);

		ticket.approveAsset(assignee, LocalDateTime.now());
		// 대여/대여연장은 구매자산팀 승인 후 실제 처리 API를 기다리기 위해 처리중으로 전환한다.
		startProcessingAfterAssetApprovalIfNeeded(ticket, companyId);
		notifyTicketRequester(ticket, "티켓이 자산 승인되었습니다.", "처리가 진행됩니다.");

		return AssetApprovalResponse.from(ticket);
	}

	@Transactional
	public AssetApprovalResponse rejectAsset(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		TicketRejectionRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member assignee = findActiveMember(authenticatedMember.id(), companyId);
		Ticket ticket = findActiveTicket(ticketId, companyId);
		validateAssetAssignable(ticket, assignee);
		validateTicketStatus(ticket, TicketStatus.DEPARTMENT_APPROVED, "부서장 승인된 티켓만 구매자산팀 반려 처리할 수 있습니다.");
		validateAssignee(ticket, assignee);

		releaseReservedRentalAssetIfNeeded(ticket, companyId);
		budgetExecutionService.releaseHoldForCancellation(ticket, companyId);
		ticket.rejectAsset(assignee, request.getRejectionReason().trim(), LocalDateTime.now());
		syncCancelledDetailStatusIfNeeded(ticket, companyId);
		syncCancelledPurchaseRequestStatusIfNeeded(ticket, companyId);
		syncCancelledRentalStatusIfNeeded(ticket, companyId);
		syncRejectedMaintenanceStatusIfNeeded(ticket, companyId);
		syncCancelledAssetReturnStatusIfNeeded(ticket, companyId);
		syncCancelledPurchaseReturnStatusIfNeeded(ticket, companyId);
		notifyTicketRequester(ticket, "티켓이 자산 반려되었습니다.", "반려 사유를 확인하세요.");

		return AssetApprovalResponse.from(ticket);
	}

	@Transactional
	public TicketProcessingStatusUpdateResponse changeProcessingStatus(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		TicketProcessingStatusUpdateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member member = findActiveMember(authenticatedMember.id(), companyId);
		Ticket ticket = findActiveTicket(ticketId, companyId);
		validateProcessingStatusChangeable(ticket, member, request.getTicketStatus());

		if (request.getTicketStatus() == TicketStatus.CANCELLED) {
			validateDirectPurchaseResultNotRegisteredForCancel(ticket, companyId);
			validatePurchasePlanNotLinkedForCancel(ticket, companyId);
		}

		ticket.changeProcessingStatus(request.getTicketStatus(), LocalDateTime.now());

		releaseReservedRentalAssetForProcessingCancelIfNeeded(ticket, companyId, request.getTicketStatus());
		releaseBudgetForProcessingCancelIfNeeded(ticket, companyId, request.getTicketStatus());
		syncAssetRequestStatusIfNeeded(ticket, companyId, request.getTicketStatus());
		syncPurchaseRequestStatusIfNeeded(ticket, companyId, request.getTicketStatus());
		syncRentalStatusIfNeeded(ticket, companyId, request.getTicketStatus());
		syncMaintenanceStatusIfNeeded(ticket, companyId, request.getTicketStatus());
		syncAssetReturnStatusIfNeeded(ticket, companyId, request.getTicketStatus());
		syncPurchaseReturnStatusIfNeeded(ticket, companyId, request.getTicketStatus());
		notifyTicketRequester(ticket, buildProcessingStatusTitle(request.getTicketStatus()), buildProcessingStatusContent(request.getTicketStatus()));
		return TicketProcessingStatusUpdateResponse.from(ticket);
	}

	public PaginationResponse<TicketListItemResponse> getTickets(
		AuthenticatedMember authenticatedMember,
		TicketSearchRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member member = findActiveMember(authenticatedMember.id(), companyId);
		applySearchScope(member, request);

		return PaginationResponse.from(ticketRepository.searchTickets(companyId, request));
	}

	public TicketStatisticsResponse getTicketStatistics(
		AuthenticatedMember authenticatedMember
	) {
		UUID companyId = authenticatedMember.companyId();
		Member member = findActiveMember(authenticatedMember.id(), companyId);
		MemberRole role = member.getRole();

		if (role == MemberRole.ADMIN || role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM) {
			return ticketRepository.getTicketStatistics(companyId, null, null, null);
		}

		if (role == MemberRole.DEPARTMENT_MANAGER) {
			return ticketRepository.getTicketStatistics(companyId, getDepartmentAndDescendantIds(member), null, member.getId());
		}

		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}

	private Member findActiveMember(UUID memberId, UUID companyId) {
		Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		return member;
	}

	private Ticket findActiveTicket(UUID ticketId, UUID companyId) {
		return ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
	}

	private void reserveRentalAssetIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.RENTAL) {
			return;
		}

		RentalTicket rentalTicket = findRentalTicket(ticket.getId(), companyId);
		if (rentalTicket.getTangibleAsset() != null) {
			return;
		}

		TangibleAsset reservedAsset = tangibleAssetRepository.findAvailableAssetsWithLock(
				companyId,
				rentalTicket.getTangibleAssetItem().getId(),
				TangibleAssetStatus.AVAILABLE,
				PageRequest.of(0, 1)
			)
			.stream()
			.findFirst()
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "대여 가능한 자산 재고가 없습니다."));

		// AVAILABLE에서 RESERVED로 바뀌면 가용 수량 조회에서 제외된다.
		reservedAsset.reserveForRental();
		rentalTicket.reserveAsset(reservedAsset);
	}

	private void assignReservedRentalAssetIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.RENTAL) {
			return;
		}

		RentalTicket rentalTicket = findRentalTicket(ticket.getId(), companyId);
		TangibleAsset asset = rentalTicket.getTangibleAsset();
		if (asset == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "예약된 대여 자산이 없습니다.");
		}

		TangibleAsset lockedAsset = tangibleAssetRepository.findWithLockByIdAndCompany_Id(asset.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));
		if (lockedAsset.getTangibleAssetStatus() != TangibleAssetStatus.RESERVED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "예약 상태의 대여 자산이 아닙니다.");
		}

		TangibleAssetAssignment assignment = TangibleAssetAssignment.builder()
			.company(ticket.getCompany())
			.tangibleAsset(lockedAsset)
			.member(ticket.getRequester())
			.department(ticket.getDepartment())
			.assignmentType(UsageType.TEMPORARY)
			.assignedAt(rentalTicket.getRentalStartDate())
			.endedAt(rentalTicket.getRequestedDueDate())
			.assignmentStatus(AssignmentStatus.ACTIVE)
			.build();
		tangibleAssetAssignmentRepository.save(assignment);

		lockedAsset.markInUse(
			ticket.getRequester(),
			ticket.getDepartment(),
			UsageType.TEMPORARY,
			resolveAssetUsageType(rentalTicket),
			rentalTicket.getRentalStartDate(),
			rentalTicket.getRequestedDueDate()
		);
		rentalTicket.markAssigned();
	}

	private void releaseReservedRentalAssetIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.RENTAL) {
			return;
		}

		RentalTicket rentalTicket = findRentalTicket(ticket.getId(), companyId);
		TangibleAsset asset = rentalTicket.getTangibleAsset();
		if (asset == null) {
			rentalTicket.cancelReservation();
			return;
		}

		TangibleAsset lockedAsset = tangibleAssetRepository.findWithLockByIdAndCompany_Id(asset.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));
		if (lockedAsset.getTangibleAssetStatus() == TangibleAssetStatus.RESERVED) {
			lockedAsset.releaseReservation();
		}
		rentalTicket.cancelReservation();
	}

	private void startProcessingAfterAssetApprovalIfNeeded(Ticket ticket, UUID companyId) {
		if (shouldStartProcessingAfterAssetApproval(ticket, companyId)) {
			ticket.changeProcessingStatus(TicketStatus.IN_PROGRESS, LocalDateTime.now());
		}
	}

	private boolean shouldStartProcessingAfterAssetApproval(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() == TicketType.ASSET_REQUEST) {
			return false;
		}
		// 대여는 할당 API, 대여연장은 반납예정일 변경 API가 완료 처리를 담당한다.
		if (ticket.getTicketType() == TicketType.RENTAL) {
			return true;
		}
		if (ticket.getTicketType() == TicketType.RENTAL_EXTENSION) {
			return true;
		}
		if (ticket.getTicketType() == TicketType.PURCHASE_REQUEST) {
			PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository
				.findByIdAndCompany_Id(ticket.getId(), companyId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
			return purchaseRequestTicket.getRequestMethod() == RequestMethod.DIRECT_PURCHASE;
		}
		return true;
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

	private void validateDepartmentApprover(Ticket ticket, Member member) {
		if (!ticket.getApprover().getId().equals(member.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateAssetAssignable(Ticket ticket, Member member) {
		if (ticketApprovalResolver.requiresAdminAssetApproval(ticket)) {
			validateRole(member, MemberRole.ADMIN);
			return;
		}

		if (ticketApprovalResolver.requiresAssetManagerApproval(ticket)) {
			validateRole(member, MemberRole.ASSET_MANAGER);
			return;
		}

		if (member.getRole() == MemberRole.ASSET_MANAGER || member.getRole() == MemberRole.ASSET_TEAM) {
			return;
		}

		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}

	private void validateRole(Member member, MemberRole expectedRole) {
		if (member.getRole() != expectedRole) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateRequester(Ticket ticket, Member member) {
		if (!ticket.getRequester().getId().equals(member.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateCancellable(Ticket ticket, Member member) {
		// 요청자 본인은 부서장 승인 전(REQUESTED)에만 취소 가능
		if (ticket.getRequester().getId().equals(member.getId())) {
			validateTicketStatus(ticket, TicketStatus.REQUESTED, "부서장 승인 전 티켓만 취소할 수 있습니다.");
			return;
		}

		if (isAssetRole(member.getRole()) && isCancellableByAssetRole(ticket.getTicketStatus())) {
			// ASSET_APPROVED 또는 IN_PROGRESS 상태는 담당자 본인만 취소 가능
			if (ticket.getTicketStatus() == TicketStatus.ASSET_APPROVED
				|| ticket.getTicketStatus() == TicketStatus.IN_PROGRESS) {
				if (ticket.getAssignee() == null || !ticket.getAssignee().getId().equals(member.getId())) {
					throw new BusinessException(ErrorCode.ACCESS_DENIED);
				}

				if (ticket.getTicketType() == TicketType.ASSET_RETURN) {
					assetReturnTicketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticket.getId(), ticket.getCompany().getId())
						.ifPresent(returnTicket -> {
							if (returnTicket.getStatus() == com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTicketStatus.COLLECTED) {
								throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 회수 처리된 반납/해지 티켓은 취소할 수 없습니다.");
							}
						});
				}
				if (ticket.getTicketType() == TicketType.PURCHASE_RETURN) {
					purchaseReturnTicketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticket.getId(), ticket.getCompany().getId())
						.ifPresent(returnTicket -> {
							if (returnTicket.getStatus() == com.ieumsae.assetieum.domain.ticket.purchasereturn.type.PurchaseReturnTicketStatus.COLLECTED) {
								throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 회수 처리된 반품/환불 티켓은 취소할 수 없습니다.");
							}
						});
				}
			}
			return;
		}

		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}

	private void validateDirectPurchaseResultNotRegisteredForCancel(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.PURCHASE_REQUEST) {
			return;
		}

		PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository
			.findByIdAndCompany_Id(ticket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		if (purchaseRequestTicket.getRequestMethod() != RequestMethod.DIRECT_PURCHASE) {
			return;
		}
		if (directPurchaseResultRepository.existsByPurchaseRequestTicket_Id(ticket.getId())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "직접구매 결과가 등록된 티켓은 취소할 수 없습니다.");
		}
	}

	private void validatePurchasePlanNotLinkedForCancel(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.PURCHASE_REQUEST && ticket.getTicketType() != TicketType.ASSET_REQUEST) {
			return;
		}
		List<com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem> linkedItems = purchasePlanItemRepository.findAllByTicket_IdAndCompany_Id(ticket.getId(), companyId);
		for (com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem item : linkedItems) {
			com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus planStatus = item.getPurchasePlan().getPurchaseRequestStatus();
			if (item.getPurchasePlan().getDeletedAt() == null &&
				planStatus != com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus.CANCELLED &&
				planStatus != com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus.REJECTED) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "현재 구매계획에 포함되어 진행 중인 요청은 취소할 수 없습니다. 구매계획을 먼저 취소하거나 품목을 제외하세요.");
			}
		}
	}

	private void validateUnassigned(Ticket ticket) {
		if (ticket.getAssignee() != null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 담당자가 지정된 티켓입니다.");
		}
	}

	private void validateAssignee(Ticket ticket, Member member) {
		if (ticket.getAssignee() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "담당자 지정 후 처리할 수 있습니다.");
		}

		if (!ticket.getAssignee().getId().equals(member.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateAssignableStatus(Ticket ticket) {
		if (ticket.getTicketStatus() == TicketStatus.REQUESTED
			|| ticket.getTicketStatus() == TicketStatus.DEPARTMENT_APPROVED) {
			return;
		}

		throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "요청 또는 부서장 승인 상태의 티켓만 담당자 지정할 수 있습니다.");
	}

	private void validateTicketStatus(Ticket ticket, TicketStatus expectedStatus, String message) {
		if (ticket.getTicketStatus() != expectedStatus) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, message);
		}
	}

	private void validateProcessingStatusChangeable(Ticket ticket, Member member, TicketStatus targetStatus) {
		// 실제 처리 API가 있는 티켓은 수동 상태 변경으로는 취소만 허용한다.
		if (ticket.getTicketType() == TicketType.MAINTENANCE_REQUEST
			|| ticket.getTicketType() == TicketType.ASSET_RETURN
			|| ticket.getTicketType() == TicketType.PURCHASE_RETURN) {
			validateManualProcessingCancelChangeable(ticket, member, targetStatus);
			return;
		}
		if (ticket.getTicketType() == TicketType.RENTAL) {
			validateRentalProcessingStatusChangeable(ticket, member, targetStatus);
			return;
		}
		// 대여연장은 반납 예정일 변경 API를 통해서만 완료 처리되므로 수동 상태 변경 불가
		if (ticket.getTicketType() == TicketType.RENTAL_EXTENSION) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "대여연장 티켓은 반납 예정일 변경 API를 통해서만 완료 처리할 수 있습니다.");
		}
		if (ticket.getTicketType() == TicketType.PURCHASE_REQUEST) {
			validatePurchaseRequestProcessingStatusChangeable(ticket, member, targetStatus);
			return;
		}
		if (ticket.getTicketType() != TicketType.ASSET_REQUEST) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자산요청 티켓만 처리상태를 변경할 수 있습니다.");
		}
		if (!isAssetRole(member.getRole())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		// 담당자 본인만 처리 가능
		if (ticket.getAssignee() == null || !ticket.getAssignee().getId().equals(member.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		if (!isProcessingTargetStatus(targetStatus)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "처리상태는 IN_PROGRESS, COMPLETED, CANCELLED만 변경할 수 있습니다.");
		}
		if (!isProcessingChangeableStatus(ticket.getTicketStatus())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매자산팀 승인 이후 티켓만 처리상태를 변경할 수 있습니다.");
		}
		if (isTerminalProcessingStatus(ticket.getTicketStatus())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "완료 또는 취소된 티켓은 처리상태를 변경할 수 없습니다.");
		}
		if (targetStatus == TicketStatus.COMPLETED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자산요청 티켓은 자산 할당 처리를 통해서만 완료할 수 있습니다.");
		}
		if (processingStatusOrder(targetStatus) <= processingStatusOrder(ticket.getTicketStatus())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "현재 처리상태 이후의 상태로만 변경할 수 있습니다.");
		}
	}


	private void validateManualProcessingCancelChangeable(Ticket ticket, Member member, TicketStatus targetStatus) {
		if (!isAssetRole(member.getRole())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		// 담당자 본인만 취소 가능
		if (ticket.getAssignee() == null || !ticket.getAssignee().getId().equals(member.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		if ((ticket.getTicketStatus() != TicketStatus.ASSET_APPROVED
			&& ticket.getTicketStatus() != TicketStatus.IN_PROGRESS)
			|| targetStatus != TicketStatus.CANCELLED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매자산팀 승인 이후 티켓은 수동으로 취소만 처리할 수 있습니다.");
		}
	}

	private void validateRentalProcessingStatusChangeable(Ticket ticket, Member member, TicketStatus targetStatus) {
		if (!isAssetRole(member.getRole())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		if (ticket.getAssignee() == null || !ticket.getAssignee().getId().equals(member.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		if ((ticket.getTicketStatus() != TicketStatus.ASSET_APPROVED
			&& ticket.getTicketStatus() != TicketStatus.IN_PROGRESS)
			|| targetStatus != TicketStatus.CANCELLED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "대여 티켓은 구매자산팀 승인 이후 취소만 처리상태 변경으로 처리할 수 있습니다.");
		}
	}

	private void validatePurchaseRequestProcessingStatusChangeable(
		Ticket ticket,
		Member member,
		TicketStatus targetStatus
	) {
		if (!isAssetRole(member.getRole())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		// 담당자 본인만 처리 가능
		if (ticket.getAssignee() == null || !ticket.getAssignee().getId().equals(member.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		if (!isProcessingTargetStatus(targetStatus)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "처리상태는 IN_PROGRESS, COMPLETED, CANCELLED만 변경할 수 있습니다.");
		}
		if (!isProcessingChangeableStatus(ticket.getTicketStatus())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매자산팀 승인 이후 구매요청만 처리상태를 변경할 수 있습니다.");
		}
		if (isTerminalProcessingStatus(ticket.getTicketStatus())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "완료 또는 취소된 티켓은 처리상태를 변경할 수 없습니다.");
		}
		if (processingStatusOrder(targetStatus) <= processingStatusOrder(ticket.getTicketStatus())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "현재 처리상태 이후의 상태로만 변경할 수 있습니다.");
		}

		PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository
			.findByIdAndCompany_Id(ticket.getId(), ticket.getCompany().getId())
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

		if (targetStatus == TicketStatus.IN_PROGRESS) {
			validatePurchaseRequestCanStartProcessing(purchaseRequestTicket);
			return;
		}
		if (targetStatus == TicketStatus.COMPLETED) {
			validatePurchaseRequestCanComplete(purchaseRequestTicket);
		}
	}

	private void validatePurchaseRequestCanStartProcessing(PurchaseRequestTicket purchaseRequestTicket) {
		if (purchaseRequestTicket.getRequestMethod() == RequestMethod.DIRECT_PURCHASE) {
			return;
		}

		List<PurchasePlanItem> linkedItems = purchasePlanItemRepository.findAllByTicket_IdAndCompany_Id(
			purchaseRequestTicket.getId(),
			purchaseRequestTicket.getCompany().getId()
		);
		if (linkedItems.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매계획에 연결된 구매요청만 처리중으로 변경할 수 있습니다.");
		}
	}

	private void validatePurchaseRequestCanComplete(PurchaseRequestTicket purchaseRequestTicket) {
		if (purchaseRequestTicket.getRequestMethod() == RequestMethod.DIRECT_PURCHASE) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "직접구매 요청은 자산 등록 및 할당 API를 통해서만 완료할 수 있습니다.");
		}
		if (purchaseRequestTicket.getRequestMethod() == RequestMethod.DIRECT_PURCHASE) {
			DirectPurchaseResult result = directPurchaseResultRepository.findByIdAndCompany_Id(
					purchaseRequestTicket.getId(), purchaseRequestTicket.getCompany().getId())
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "직접구매 결과 등록 후 완료 처리할 수 있습니다."));
			// [수정] 자산팀이 증빙을 확인(CONFIRMED)해야 최종 완료 가능
			if (result.getConfirmationStatus() != ConfirmationStatus.CONFIRMED) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "직접구매 결과 확인(CONFIRMED) 후 완료 처리할 수 있습니다.");
			}
			return;
		}

		List<PurchasePlanItem> linkedItems = purchasePlanItemRepository.findAllByTicket_IdAndCompany_Id(
			purchaseRequestTicket.getId(),
			purchaseRequestTicket.getCompany().getId()
		);
		boolean allRegistered = !linkedItems.isEmpty()
			&& linkedItems.stream()
				.allMatch(item -> item.getPurchasePlanItemStatus() == PurchasePlanItemStatus.ASSET_REGISTERED);
		if (!allRegistered) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매계획 품목 자산등록이 완료된 구매요청만 완료 처리할 수 있습니다.");
		}
	}

	private void releaseReservedRentalAssetForProcessingCancelIfNeeded(
		Ticket ticket,
		UUID companyId,
		TicketStatus targetStatus
	) {
		if (ticket.getTicketType() != TicketType.RENTAL || targetStatus != TicketStatus.CANCELLED) {
			return;
		}
		// 처리중 취소 시 선점해둔 대여 자산을 다시 AVAILABLE로 복구한다.
		releaseReservedRentalAssetIfNeeded(ticket, companyId);
	}

	private void syncCancelledDetailStatusIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.ASSET_REQUEST) {
			return;
		}
		syncAssetRequestStatus(ticket, companyId, TicketStatus.CANCELLED);
	}

	private void releaseBudgetForProcessingCancelIfNeeded(
		Ticket ticket,
		UUID companyId,
		TicketStatus targetStatus
	) {
		if (targetStatus != TicketStatus.CANCELLED) {
			return;
		}
		budgetExecutionService.releaseHoldForCancellation(ticket, companyId);
	}

	private void syncCancelledPurchaseRequestStatusIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.PURCHASE_REQUEST) {
			return;
		}

		PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository
			.findByIdAndCompany_Id(ticket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		purchaseRequestTicket.cancel();
	}

	private void syncCancelledRentalStatusIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.RENTAL
			&& ticket.getTicketType() != TicketType.RENTAL_EXTENSION) {
			return;
		}
		RentalTicket rentalTicket = findRentalTicket(ticket.getId(), companyId);
		rentalTicket.cancelReservation();
	}

	private void syncRentalStatusIfNeeded(Ticket ticket, UUID companyId, TicketStatus targetStatus) {
		if (targetStatus == TicketStatus.CANCELLED) {
			syncCancelledRentalStatusIfNeeded(ticket, companyId);
		}
	}

	private void syncCancelledMaintenanceStatusIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.MAINTENANCE_REQUEST) {
			return;
		}
		MaintenanceTicket maintenanceTicket = maintenanceTicketRepository
			.findByIdAndCompany_IdAndDeletedAtIsNull(ticket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		maintenanceTicket.getTangibleAsset().restoreInUseAfterTicketCancel();
		maintenanceTicket.cancel();
	}

	private void syncRejectedMaintenanceStatusIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.MAINTENANCE_REQUEST) {
			return;
		}
		MaintenanceTicket maintenanceTicket = maintenanceTicketRepository
			.findByIdAndCompany_IdAndDeletedAtIsNull(ticket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		maintenanceTicket.getTangibleAsset().restoreInUseAfterTicketCancel();
		maintenanceTicket.cancel();
	}

	private void syncCancelledAssetReturnStatusIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.ASSET_RETURN) {
			return;
		}
		AssetReturnTicket assetReturnTicket = assetReturnTicketRepository
			.findByIdAndCompany_IdAndDeletedAtIsNull(ticket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		cancelAssetReturnDetail(assetReturnTicket);
	}

	private void syncRejectedAssetReturnStatusIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.ASSET_RETURN) {
			return;
		}
		AssetReturnTicket assetReturnTicket = assetReturnTicketRepository
			.findByIdAndCompany_IdAndDeletedAtIsNull(ticket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		cancelAssetReturnDetail(assetReturnTicket);
	}

	private void syncMaintenanceStatusIfNeeded(Ticket ticket, UUID companyId, TicketStatus targetStatus) {
		if (targetStatus == TicketStatus.CANCELLED) {
			syncCancelledMaintenanceStatusIfNeeded(ticket, companyId);
		}
	}

	private void syncAssetReturnStatusIfNeeded(Ticket ticket, UUID companyId, TicketStatus targetStatus) {
		if (targetStatus == TicketStatus.CANCELLED) {
			syncCancelledAssetReturnStatusIfNeeded(ticket, companyId);
		}
	}

	private void syncCancelledPurchaseReturnStatusIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.PURCHASE_RETURN) {
			return;
		}
		PurchaseReturnTicket purchaseReturnTicket = purchaseReturnTicketRepository
			.findByIdAndCompany_IdAndDeletedAtIsNull(ticket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		cancelPurchaseReturnDetail(purchaseReturnTicket);
	}

	private void syncRejectedPurchaseReturnStatusIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.PURCHASE_RETURN) {
			return;
		}
		PurchaseReturnTicket purchaseReturnTicket = purchaseReturnTicketRepository
			.findByIdAndCompany_IdAndDeletedAtIsNull(ticket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		cancelPurchaseReturnDetail(purchaseReturnTicket);
	}

	private void syncPurchaseReturnStatusIfNeeded(Ticket ticket, UUID companyId, TicketStatus targetStatus) {
		if (targetStatus == TicketStatus.CANCELLED) {
			syncCancelledPurchaseReturnStatusIfNeeded(ticket, companyId);
		}
	}

	private void cancelAssetReturnDetail(AssetReturnTicket assetReturnTicket) {
		if (assetReturnTicket.getStatus() == AssetReturnTicketStatus.COMPLETED
			|| assetReturnTicket.getStatus() == AssetReturnTicketStatus.CANCELLED) {
			return;
		}

		// 회수 전 취소는 기존 사용 상태로 복구하고, 회수 후 취소는 회수 결과를 유지한다.
		if (assetReturnTicket.getTangibleAsset() != null) {
			if (assetReturnTicket.getStatus() == AssetReturnTicketStatus.COLLECTED) {
				assetReturnTicket.getTangibleAsset().completeReturn();
			} else {
				assetReturnTicket.getTangibleAsset().restoreInUseAfterTicketCancel();
			}
		} else if (assetReturnTicket.getIntangibleAsset() != null) {
			if (assetReturnTicket.getStatus() == AssetReturnTicketStatus.COLLECTED) {
				assetReturnTicket.getIntangibleAsset().cancel();
			} else {
				assetReturnTicket.getIntangibleAsset().restoreInUseAfterTicketCancel();
			}
		}
		assetReturnTicket.cancel();
	}

	private void cancelPurchaseReturnDetail(PurchaseReturnTicket purchaseReturnTicket) {
		if (purchaseReturnTicket.getStatus() == PurchaseReturnTicketStatus.COMPLETED
			|| purchaseReturnTicket.getStatus() == PurchaseReturnTicketStatus.CANCELLED) {
			return;
		}

		// 반품도 회수 전 취소는 사용 상태 복구, 회수 후 취소는 회수된 자산 상태를 유지한다.
		if (purchaseReturnTicket.getTangibleAsset() != null) {
			if (purchaseReturnTicket.getStatus() == PurchaseReturnTicketStatus.COLLECTED
				|| purchaseReturnTicket.getStatus() == PurchaseReturnTicketStatus.SHIPPED) {
				purchaseReturnTicket.getTangibleAsset().completeReturn();
			} else {
				purchaseReturnTicket.getTangibleAsset().restoreInUseAfterTicketCancel();
			}
		} else if (purchaseReturnTicket.getIntangibleAsset() != null) {
			if (purchaseReturnTicket.getStatus() == PurchaseReturnTicketStatus.COLLECTED
				|| purchaseReturnTicket.getStatus() == PurchaseReturnTicketStatus.SHIPPED) {
				purchaseReturnTicket.getIntangibleAsset().cancel();
			} else {
				purchaseReturnTicket.getIntangibleAsset().restoreInUseAfterTicketCancel();
			}
		}
		purchaseReturnTicket.cancel();
	}

	private void syncAssetRequestStatusIfNeeded(Ticket ticket, UUID companyId, TicketStatus targetStatus) {
		if (ticket.getTicketType() != TicketType.ASSET_REQUEST) {
			return;
		}
		syncAssetRequestStatus(ticket, companyId, targetStatus);
	}

	private void syncPurchaseRequestStatusIfNeeded(Ticket ticket, UUID companyId, TicketStatus targetStatus) {
		if (ticket.getTicketType() != TicketType.PURCHASE_REQUEST) {
			return;
		}

		PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository
			.findByIdAndCompany_Id(ticket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

		if (targetStatus == TicketStatus.CANCELLED) {
			purchaseRequestTicket.cancel();
			return;
		}
		if (targetStatus == TicketStatus.COMPLETED) {
			purchaseRequestTicket.complete();
			return;
		}
		if (targetStatus == TicketStatus.IN_PROGRESS
			&& purchaseRequestTicket.getRequestMethod() == RequestMethod.TEAM_PURCHASE) {
			purchaseRequestTicket.markOrdered();
		}
	}

	private void syncAssetRequestStatus(Ticket ticket, UUID companyId, TicketStatus targetStatus) {
		AssetRequestTicket assetRequestTicket = assetRequestTicketRepository
			.findByIdAndCompany_IdAndDeletedAtIsNull(ticket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

		if (targetStatus == TicketStatus.IN_PROGRESS) {
			return;
		}
		if (targetStatus == TicketStatus.COMPLETED) {
			assetRequestTicket.complete();
			return;
		}
		assetRequestTicket.cancel();
	}

	private boolean isAssetRole(MemberRole role) {
		return role == MemberRole.ADMIN || role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM;
	}

	private boolean isProcessingTargetStatus(TicketStatus status) {
		return status == TicketStatus.IN_PROGRESS
			|| status == TicketStatus.COMPLETED
			|| status == TicketStatus.CANCELLED;
	}

	private boolean isProcessingChangeableStatus(TicketStatus status) {
		return status == TicketStatus.ASSET_APPROVED || isProcessingTargetStatus(status);
	}

	private boolean isTerminalProcessingStatus(TicketStatus status) {
		return status == TicketStatus.COMPLETED || status == TicketStatus.CANCELLED;
	}

	private int processingStatusOrder(TicketStatus status) {
		return switch (status) {
			case ASSET_APPROVED -> 1;
			case IN_PROGRESS -> 2;
			case COMPLETED, CANCELLED -> 3;
			default -> 0;
		};
	}

	private boolean isCancellableByAssetRole(TicketStatus status) {
		return status == TicketStatus.REQUESTED
			|| status == TicketStatus.DEPARTMENT_APPROVED
			|| status == TicketStatus.IN_PROGRESS
			|| status == TicketStatus.ASSET_APPROVED;
	}

	private void applySearchScope(Member member, TicketSearchRequest request) {
		MemberRole role = member.getRole();

		if (role == MemberRole.ADMIN || role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM) {
			validateRequesterScope(member.getCompany().getId(), request.getDepartmentId(), request.getRequesterId());
			return;
		}

		if (role == MemberRole.DEPARTMENT_MANAGER) {
			List<UUID> departmentIds = getDepartmentAndDescendantIds(member);
			request.setDepartmentId(null);
			request.setDepartmentIds(departmentIds);
			request.setApproverId(member.getId());
			validateRequesterScope(member.getCompany().getId(), departmentIds, request.getRequesterId());
			return;
		}

		request.setDepartmentId(member.getDepartment().getId());
		request.setRequesterId(member.getId());
	}

	private void validateRequesterScope(UUID companyId, UUID departmentId, UUID requesterId) {
		if (requesterId == null) {
			return;
		}

		Member requester = findActiveMember(requesterId, companyId);
		if (departmentId != null && !requester.getDepartment().getId().equals(departmentId)) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}
	}

	private void validateRequesterScope(UUID companyId, List<UUID> departmentIds, UUID requesterId) {
		if (requesterId == null) {
			return;
		}

		Member requester = findActiveMember(requesterId, companyId);
		if (departmentIds != null && !departmentIds.isEmpty() && !departmentIds.contains(requester.getDepartment().getId())) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}
	}

	private List<UUID> getDepartmentAndDescendantIds(Member member) {
		UUID rootDepartmentId = member.getDepartment().getId();
		List<Department> departments = departmentRepository.findAllByCompany_IdAndDeletedAtIsNullOrderByCreatedAtAsc(
			member.getCompany().getId()
		);
		Set<UUID> departmentIds = new LinkedHashSet<>();
		departmentIds.add(rootDepartmentId);

		boolean added;
		do {
			added = false;
			for (Department department : departments) {
				Department parentDepartment = department.getParentDepartment();
				if (parentDepartment != null
					&& departmentIds.contains(parentDepartment.getId())
					&& departmentIds.add(department.getId())) {
					added = true;
				}
			}
		} while (added);

		return new ArrayList<>(departmentIds);
	}

	private void notifyTicketRequester(Ticket ticket, String title, String content) {
		Member requester = ticket.getRequester();
		if (requester == null || !requester.isActive()) {
			return;
		}

		notificationService.createNotification(
			requester,
			NotificationType.TICKET_STATUS_CHANGED,
			title,
			content,
			NotificationTargetType.TICKET,
			ticket.getId()
		);
	}

	private String buildProcessingStatusTitle(TicketStatus status) {
		return switch (status) {
			case IN_PROGRESS -> "티켓이 처리 중으로 변경되었습니다.";
			case COMPLETED -> "티켓이 완료되었습니다.";
			case CANCELLED -> "티켓이 취소되었습니다.";
			default -> "티켓 상태가 변경되었습니다.";
		};
	}

	private String buildProcessingStatusContent(TicketStatus status) {
		return switch (status) {
			case IN_PROGRESS -> "처리 담당자가 업무를 진행 중입니다.";
			case COMPLETED -> "요청 처리가 완료되었습니다.";
			case CANCELLED -> "요청이 취소되었습니다.";
			default -> "티켓 상태 변경 내용을 확인하세요.";
		};
	}
}
