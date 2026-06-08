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
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
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
		Member requester = validateSuperAdmin(authenticatedMember);

		Page<MemberListItemResponse> members = memberRepository.searchMembers(
			requester.getCompany().getId(),
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
		Member requester = validateSuperAdmin(authenticatedMember);
		Company company = requester.getCompany();
		Department department = findActiveDepartment(request.getDepartmentId(), company.getId());
		validateMemberNoNotDuplicated(company.getId(), request.getMemberNo());
		validateEmailNotDuplicated(company.getId(), request.getEmail());

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

		return MemberCreateResponse.from(member);
	}

	@Transactional
	public MemberDepartmentUpdateResponse updateMemberDepartment(
		AuthenticatedMember authenticatedMember,
		UUID memberId,
		MemberDepartmentUpdateRequest request
	) {
		Member requester = validateSuperAdmin(authenticatedMember);
		UUID companyId = requester.getCompany().getId();
		Member member = findActiveMember(memberId, companyId);
		Department previousDepartment = member.getDepartment();
		Department currentDepartment = findActiveDepartment(request.getDepartmentId(), companyId);

		member.changeDepartment(currentDepartment);

		return MemberDepartmentUpdateResponse.from(member, previousDepartment);
	}

	private Member validateSuperAdmin(AuthenticatedMember authenticatedMember) {
		Member member = memberRepository.findById(authenticatedMember.id())
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		if (member.getRole() != MemberRole.SUPER_ADMIN) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		return member;
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
		if (memberRepository.existsByCompany_IdAndMemberNoAndDeletedAtIsNull(companyId, memberNo)) {
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

	private String normalizeKeyword(String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return null;
		}

		return keyword.trim();
	}
}
