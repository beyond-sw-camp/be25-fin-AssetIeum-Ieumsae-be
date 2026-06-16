package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketApprovalResolver {

	private final MemberRepository memberRepository;

	public Member resolveDepartmentApprover(Member requester) {
		MemberRole role = requester.getRole();

		if (role == MemberRole.DEPARTMENT_MANAGER || role == MemberRole.ASSET_MANAGER) {
			return resolveUpperDepartmentManagerOrAdmin(requester);
		}

		return resolveCurrentDepartmentManager(requester);
	}

	public boolean requiresAdminAssetApproval(Ticket ticket) {
		return ticket.getRequester().getRole() == MemberRole.ASSET_MANAGER;
	}

	public boolean requiresAssetManagerApproval(Ticket ticket) {
		return ticket.getRequester().getRole() == MemberRole.ASSET_TEAM;
	}

	private Member resolveUpperDepartmentManagerOrAdmin(Member requester) {
		Department parentDepartment = requester.getDepartment().getParentDepartment();
		if (parentDepartment != null) {
			return validateManager(parentDepartment.getDepartmentManager());
		}

		return findActiveAdmin(requester);
	}

	private Member resolveCurrentDepartmentManager(Member requester) {
		return validateManager(requester.getDepartment().getDepartmentManager());
	}

	private Member validateManager(Member manager) {
		if (manager == null || !manager.isActive() || !isManagerRole(manager.getRole())) {
			throw new BusinessException(ErrorCode.INVALID_DEPARTMENT_MANAGER, "유효한 부서장이 지정되어 있지 않습니다.");
		}

		return manager;
	}

	private Member findActiveAdmin(Member requester) {
		Member admin = memberRepository.findFirstByCompany_IdAndRoleAndDeletedAtIsNull(
				requester.getCompany().getId(),
				MemberRole.ADMIN
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "회사 관리자를 찾을 수 없습니다."));

		if (!admin.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER, "회사 관리자가 활성 상태가 아닙니다.");
		}

		return admin;
	}

	private boolean isManagerRole(MemberRole role) {
		return role == MemberRole.DEPARTMENT_MANAGER || role == MemberRole.ASSET_MANAGER;
	}
}
