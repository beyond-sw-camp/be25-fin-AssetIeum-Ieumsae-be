package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.entity.TicketAssignmentTarget;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketAssignmentTargetRepository;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketAssignmentTargetService {

	private final TicketAssignmentTargetRepository ticketAssignmentTargetRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void saveRequiredTargets(
		UUID companyId,
		Ticket ticket,
		List<UUID> memberIds,
		int requiredCount,
		RequestedUsageType requestedUsageType
	) {
		validateUsageTargetPolicy(memberIds, requestedUsageType);
		if (requestedUsageType == RequestedUsageType.DEPARTMENT) {
			return;
		}
		if (memberIds == null || memberIds.isEmpty()) {
			if (requiredCount > 1) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "수량이 2개 이상이면 배정 대상자를 입력해야 합니다.");
			}
			saveRequesterFallbackTargets(ticket, requiredCount);
			return;
		}
		if (memberIds.size() != requiredCount) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배정 대상자 수는 요청 수량과 일치해야 합니다.");
		}
		saveTargets(companyId, ticket, memberIds);
	}

	@Transactional
	public void saveOptionalTargets(
		UUID companyId,
		Ticket ticket,
		List<UUID> memberIds,
		RequestedUsageType requestedUsageType
	) {
		validateUsageTargetPolicy(memberIds, requestedUsageType);
		if (memberIds == null || memberIds.isEmpty()) {
			return;
		}
		saveTargets(companyId, ticket, memberIds);
	}

	@Transactional
	public void saveRequiredAtLeastOneTarget(
		UUID companyId,
		Ticket ticket,
		List<UUID> memberIds,
		RequestedUsageType requestedUsageType
	) {
		validateUsageTargetPolicy(memberIds, requestedUsageType);
		if (requestedUsageType == RequestedUsageType.DEPARTMENT) {
			return;
		}
		if (memberIds == null || memberIds.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배정 대상자를 입력해야 합니다.");
		}
		saveTargets(companyId, ticket, memberIds);
	}

	@Transactional
	public void replaceRequiredTargetsWithinCapacity(
		UUID companyId,
		Ticket ticket,
		List<UUID> memberIds,
		RequestedUsageType requestedUsageType,
		int capacity
	) {
		validateUsageTargetPolicy(memberIds, requestedUsageType);
		if (requestedUsageType == RequestedUsageType.DEPARTMENT) {
			deleteTargets(companyId, ticket);
			return;
		}
		if (memberIds == null || memberIds.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배정 대상자를 입력해야 합니다.");
		}
		if (memberIds.size() > capacity) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배정 대상자 수가 사용 가능한 좌석 수를 초과했습니다.");
		}
		validateNoDuplicateMembers(memberIds);
		deleteTargets(companyId, ticket);
		saveTargets(companyId, ticket, memberIds);
	}

	@Transactional
	public void replaceRequiredTargets(
		UUID companyId,
		Ticket ticket,
		List<UUID> memberIds,
		RequestedUsageType requestedUsageType,
		int requiredCount
	) {
		validateUsageTargetPolicy(memberIds, requestedUsageType);
		if (requestedUsageType == RequestedUsageType.DEPARTMENT) {
			deleteTargets(companyId, ticket);
			return;
		}
		if (memberIds == null || memberIds.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배정 대상자를 입력해야 합니다.");
		}
		if (memberIds.size() != requiredCount) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배정 대상자 수는 요청 수량과 일치해야 합니다.");
		}
		validateNoDuplicateMembers(memberIds);
		deleteTargets(companyId, ticket);
		saveTargets(companyId, ticket, memberIds);
	}

	@Transactional(readOnly = true)
	public List<TicketAssignmentTarget> findTargets(UUID companyId, Ticket ticket) {
		return ticketAssignmentTargetRepository.findAllByTicket_IdAndCompany_IdOrderByCreatedAtAsc(
			ticket.getId(),
			companyId
		);
	}

	public List<Member> resolveAssigneesOrRequesterFallback(UUID companyId, Ticket ticket, int quantity) {
		List<TicketAssignmentTarget> targets = findTargets(companyId, ticket);
		if (targets.isEmpty()) {
			return createRequesterFallbackAssignees(ticket.getRequester(), quantity);
		}
		if (targets.size() != quantity) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배정 대상자 수는 요청 수량과 일치해야 합니다.");
		}
		return targets.stream()
			.map(TicketAssignmentTarget::getMember)
			.toList();
	}

	public List<TicketAssignmentTarget> resolveTargetsOrEmpty(UUID companyId, Ticket ticket) {
		return findTargets(companyId, ticket);
	}

	public void validateCapacity(List<TicketAssignmentTarget> targets, int capacity) {
		if (targets.size() > capacity) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배정 대상자 수가 사용 가능한 좌석 수를 초과했습니다.");
		}
	}

	@Transactional
	public void markAssigned(TicketAssignmentTarget target, AssetType assetType, UUID assetId, LocalDateTime assignedAt) {
		target.markAssigned(assetType, assetId, assignedAt);
	}

	private void saveTargets(UUID companyId, Ticket ticket, List<UUID> memberIds) {
		validateNoDuplicateMembers(memberIds);
		List<TicketAssignmentTarget> targets = new ArrayList<>();
		for (UUID memberId : memberIds) {
			Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
			if (!member.isActive()) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "활성 상태의 구성원만 배정 대상자로 지정할 수 있습니다.");
			}
			if (!member.getDepartment().getId().equals(ticket.getDepartment().getId())) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배정 대상자는 요청자와 같은 부서에 속해야 합니다.");
			}
			targets.add(TicketAssignmentTarget.create(ticket, member));
		}
		ticketAssignmentTargetRepository.saveAll(targets);
	}

	private void validateUsageTargetPolicy(List<UUID> memberIds, RequestedUsageType requestedUsageType) {
		if (requestedUsageType == RequestedUsageType.DEPARTMENT && memberIds != null && !memberIds.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "부서용 요청에는 개인 배정 대상자를 지정할 수 없습니다.");
		}
	}

	private void deleteTargets(UUID companyId, Ticket ticket) {
		ticketAssignmentTargetRepository.deleteAllByTicket_IdAndCompany_Id(ticket.getId(), companyId);
		ticketAssignmentTargetRepository.flush();
	}

	private void saveRequesterFallbackTargets(Ticket ticket, int requiredCount) {
		if (requiredCount != 1) {
			return;
		}
		ticketAssignmentTargetRepository.save(TicketAssignmentTarget.create(ticket, ticket.getRequester()));
	}

	private void validateNoDuplicateMembers(List<UUID> memberIds) {
		Set<UUID> uniqueMemberIds = new HashSet<>(memberIds);
		if (uniqueMemberIds.size() != memberIds.size()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배정 대상자를 중복으로 지정할 수 없습니다.");
		}
	}

	private List<Member> createRequesterFallbackAssignees(Member requester, int quantity) {
		List<Member> assignees = new ArrayList<>();
		for (int i = 0; i < quantity; i++) {
			assignees.add(requester);
		}
		return assignees;
	}
}
