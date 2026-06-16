package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.ticket.common.dto.AssetApprovalResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.DepartmentApprovalResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketAssigneeResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketCancelResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketListItemResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketRejectionRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketSearchRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketStatisticsResponse;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

	private final TicketRepository ticketRepository;
	private final MemberRepository memberRepository;
	private final DepartmentRepository departmentRepository;
	private final TicketApprovalResolver ticketApprovalResolver;

	@Transactional
	public TicketAssigneeResponse assignMe(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member assignee = findActiveMember(authenticatedMember.id(), companyId);
		Ticket ticket = findActiveTicket(ticketId, companyId);
		validateAssetAssignable(ticket, assignee);
		validateTicketStatus(ticket, TicketStatus.DEPARTMENT_APPROVED, "부서장 승인된 티켓만 담당자 지정할 수 있습니다.");
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
		Member requester = findActiveMember(authenticatedMember.id(), companyId);
		Ticket ticket = findActiveTicket(ticketId, companyId);
		validateRequester(ticket, requester);
		validateTicketStatus(ticket, TicketStatus.REQUESTED, "부서장 승인 전 티켓만 취소할 수 있습니다.");

		ticket.cancel(LocalDateTime.now());

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

		ticket.rejectAsset(assignee, request.getRejectionReason().trim(), LocalDateTime.now());

		return AssetApprovalResponse.from(ticket);
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
		return ticketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
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

	private void validateTicketStatus(Ticket ticket, TicketStatus expectedStatus, String message) {
		if (ticket.getTicketStatus() != expectedStatus) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, message);
		}
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
