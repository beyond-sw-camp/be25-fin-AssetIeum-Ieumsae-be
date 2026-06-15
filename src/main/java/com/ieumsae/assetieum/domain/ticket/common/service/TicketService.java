package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketListItemResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketSearchRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketStatisticsResponse;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
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
			return ticketRepository.getTicketStatistics(companyId, null, null);
		}

		if (role == MemberRole.DEPARTMENT_MANAGER) {
			return ticketRepository.getTicketStatistics(companyId, member.getDepartment().getId(), null);
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

	private void applySearchScope(Member member, TicketSearchRequest request) {
		MemberRole role = member.getRole();

		if (role == MemberRole.ADMIN || role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM) {
			validateRequesterScope(member.getCompany().getId(), request.getDepartmentId(), request.getRequesterId());
			return;
		}

		if (role == MemberRole.DEPARTMENT_MANAGER) {
			request.setDepartmentId(member.getDepartment().getId());
			validateRequesterScope(member.getCompany().getId(), member.getDepartment().getId(), request.getRequesterId());
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
}
