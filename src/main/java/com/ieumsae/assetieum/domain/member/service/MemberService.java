package com.ieumsae.assetieum.domain.member.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.member.dto.MemberCreateRequest;
import com.ieumsae.assetieum.domain.member.dto.MemberCreateResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberDepartmentUpdateRequest;
import com.ieumsae.assetieum.domain.member.dto.MemberDepartmentUpdateResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberListItemResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberSearchRequest;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final DepartmentRepository departmentRepository;
	private final PasswordEncoder passwordEncoder;

	public PaginationResponse<MemberListItemResponse> getMembers(
		AuthenticatedMember authenticatedMember,
		MemberSearchRequest request
	) {
		validateAdmin(authenticatedMember);
		UUID companyId = authenticatedMember.companyId();
		validateSearchDepartment(request.getDepartmentId(), companyId);

		Page<MemberListItemResponse> members = memberRepository.searchMembers(
			companyId,
			normalizeKeyword(request.getKeyword()),
			request.getDepartmentId(),
			request.getStatus(),
			request.toPageable()
		).map(MemberListItemResponse::from);

		return PaginationResponse.from(members);
	}

	@Transactional
	public MemberCreateResponse createMember(
		AuthenticatedMember authenticatedMember,
		MemberCreateRequest request
	) {
		validateAdmin(authenticatedMember);
		UUID companyId = authenticatedMember.companyId();
		Department department = findActiveDepartment(request.getDepartmentId(), companyId);
		Company company = department.getCompany();
		validateMemberNoNotDuplicated(companyId, request.getMemberNo());
		validateEmailNotDuplicated(companyId, request.getEmail());
		validateAssignableRole(request.getRole());
		validateDepartmentManagerAssignable(department, request.getRole());

		Member member = memberRepository.save(Member.builder()
			.company(company)
			.department(department)
			.memberNo(request.getMemberNo())
			.password(passwordEncoder.encode(request.getMemberNo()))
			.name(request.getName())
			.role(request.getRole())
			.status(MemberStatus.ACTIVE)
			.email(request.getEmail())
			.build());

		assignDepartmentManagerIfNeeded(department, member);

		return MemberCreateResponse.from(member);
	}

	@Transactional
	public MemberDepartmentUpdateResponse updateMemberDepartment(
		AuthenticatedMember authenticatedMember,
		UUID memberId,
		MemberDepartmentUpdateRequest request
	) {
		validateAdmin(authenticatedMember);
		UUID companyId = authenticatedMember.companyId();
		Member member = findActiveMember(memberId, companyId);
		Department previousDepartment = member.getDepartment();
		Department currentDepartment = findActiveDepartment(request.getDepartmentId(), companyId);

		member.changeDepartment(currentDepartment);

		return MemberDepartmentUpdateResponse.from(member, previousDepartment);
	}

	private void validateAdmin(AuthenticatedMember authenticatedMember) {
		Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
				authenticatedMember.id(),
				authenticatedMember.companyId()
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		if (member.getRole() != MemberRole.ADMIN) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private Department findActiveDepartment(UUID departmentId, UUID companyId) {
		return departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(departmentId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
	}

	private Member findActiveMember(UUID memberId, UUID companyId) {
		Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		return member;
	}

	private void validateMemberNoNotDuplicated(UUID companyId, String memberNo) {
		if (memberRepository.existsByCompany_IdAndMemberNo(companyId, memberNo)) {
			throw new BusinessException(ErrorCode.MEMBER_ALREADY_EXISTS);
		}
	}

	private void validateEmailNotDuplicated(UUID companyId, String email) {
		if (!StringUtils.hasText(email)) {
			return;
		}

		if (memberRepository.existsByCompany_IdAndEmailAndDeletedAtIsNull(companyId, email)) {
			throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_EXISTS);
		}
	}

	private void validateSearchDepartment(UUID departmentId, UUID companyId) {
		if (departmentId == null) {
			return;
		}

		findActiveDepartment(departmentId, companyId);
	}

	private void validateAssignableRole(MemberRole role) {
		if (role == MemberRole.SUPER_ADMIN) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateDepartmentManagerAssignable(Department department, MemberRole role) {
		if (role != MemberRole.DEPARTMENT_MANAGER) {
			return;
		}

		if (department.getDepartmentManager() != null) {
			throw new BusinessException(ErrorCode.INVALID_DEPARTMENT_MANAGER, "이미 부서장이 지정된 부서입니다.");
		}
	}

	private void assignDepartmentManagerIfNeeded(Department department, Member member) {
		if (member.getRole() != MemberRole.DEPARTMENT_MANAGER) {
			return;
		}

		department.changeDepartmentManager(member);
	}

	private String normalizeKeyword(String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return null;
		}

		return keyword.trim();
	}
}
