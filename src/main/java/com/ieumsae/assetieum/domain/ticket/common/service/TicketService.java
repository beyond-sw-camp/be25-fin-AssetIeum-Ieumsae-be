package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.domain.budget.budget.service.BudgetExecutionService;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
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
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.DirectPurchaseResultRepository;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.PurchaseRequestTicketRepository;
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
	private final DirectPurchaseResultRepository directPurchaseResultRepository;
	private final PurchasePlanItemRepository purchasePlanItemRepository;
	private final MaintenanceTicketRepository maintenanceTicketRepository;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final BudgetExecutionService budgetExecutionService;

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

		releaseReservedRentalAssetIfNeeded(ticket, companyId);
		budgetExecutionService.releaseHoldForCancellation(ticket, companyId);
		ticket.cancel(LocalDateTime.now());
		syncCancelledDetailStatusIfNeeded(ticket, companyId);
		syncCancelledPurchaseRequestStatusIfNeeded(ticket, companyId);
		syncCancelledRentalStatusIfNeeded(ticket, companyId);
		syncCancelledMaintenanceStatusIfNeeded(ticket, companyId);

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

		return DepartmentApprovalResponse.from(ticket);
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
		syncCancelledPurchaseRequestStatusIfNeeded(ticket, companyId);

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
		releaseReservedRentalAssetForProcessingCancelIfNeeded(ticket, companyId, request.getTicketStatus());
		releaseBudgetForProcessingCancelIfNeeded(ticket, companyId, request.getTicketStatus());
		ticket.changeProcessingStatus(request.getTicketStatus(), LocalDateTime.now());
		syncAssetRequestStatusIfNeeded(ticket, companyId, request.getTicketStatus());
		syncPurchaseRequestStatusIfNeeded(ticket, companyId, request.getTicketStatus());

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
		if (ticket.getRequester().getId().equals(member.getId())) {
			validateTicketStatus(ticket, TicketStatus.REQUESTED, "부서장 승인 전 티켓만 취소할 수 있습니다.");
			return;
		}

		if (isAssetRole(member.getRole()) && isCancellableByAssetRole(ticket.getTicketStatus())) {
			return;
		}

		throw new BusinessException(ErrorCode.ACCESS_DENIED);
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
		if (ticket.getTicketType() == TicketType.RENTAL
			|| ticket.getTicketType() == TicketType.ASSET_REQUEST
			|| ticket.getTicketType() == TicketType.PURCHASE_REQUEST) {
			validateManualProcessingCancelChangeable(ticket, member, targetStatus);
			return;
		}
		if (ticket.getTicketType() == TicketType.RENTAL) {
			validateRentalProcessingStatusChangeable(ticket, member, targetStatus);
			return;
		}
		if (ticket.getTicketType() == TicketType.PURCHASE_REQUEST) {
			validatePurchaseRequestProcessingStatusChangeable(ticket, member, targetStatus);
			return;
		}
		if (ticket.getTicketType() != TicketType.ASSET_REQUEST
			&& ticket.getTicketType() != TicketType.RENTAL) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자산요청 티켓만 처리상태를 변경할 수 있습니다.");
		}
		if (!isAssetRole(member.getRole())) {
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
		if (ticket.getTicketStatus() == TicketStatus.ASSET_APPROVED && targetStatus == TicketStatus.COMPLETED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "처리중 상태 이후 완료할 수 있습니다.");
		}
		if (processingStatusOrder(targetStatus) <= processingStatusOrder(ticket.getTicketStatus())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "현재 처리상태 이후의 상태로만 변경할 수 있습니다.");
		}
	}

	private void validateManualProcessingCancelChangeable(Ticket ticket, Member member, TicketStatus targetStatus) {
		if (!isAssetRole(member.getRole())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		if (ticket.getAssignee() != null && !ticket.getAssignee().getId().equals(member.getId())) {
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
			if (!directPurchaseResultRepository.existsByPurchaseRequestTicket_Id(purchaseRequestTicket.getId())) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "직접구매 결과 등록 후 완료 처리할 수 있습니다.");
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

	private void syncCancelledMaintenanceStatusIfNeeded(Ticket ticket, UUID companyId) {
		if (ticket.getTicketType() != TicketType.MAINTENANCE_REQUEST) {
			return;
		}
		MaintenanceTicket maintenanceTicket = maintenanceTicketRepository
			.findByIdAndCompany_IdAndDeletedAtIsNull(ticket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		maintenanceTicket.cancel();
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
}
