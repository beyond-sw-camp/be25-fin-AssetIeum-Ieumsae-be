package com.ieumsae.assetieum.domain.department.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.department.dto.DepartmentCreateRequest;
import com.ieumsae.assetieum.domain.department.dto.DepartmentCreateResponse;
import com.ieumsae.assetieum.domain.department.dto.DepartmentDeleteResponse;
import com.ieumsae.assetieum.domain.department.dto.DepartmentDetailResponse;
import com.ieumsae.assetieum.domain.department.dto.DepartmentListResponse;
import com.ieumsae.assetieum.domain.department.dto.DepartmentTreeResponse;
import com.ieumsae.assetieum.domain.department.dto.DepartmentUpdateRequest;
import com.ieumsae.assetieum.domain.department.dto.DepartmentUpdateResponse;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

	private final DepartmentRepository departmentRepository;
	private final MemberRepository memberRepository;

	public DepartmentListResponse getDepartments(AuthenticatedMember authenticatedMember) {
		Member requester = validateSuperAdmin(authenticatedMember);
		UUID companyId = requester.getCompany().getId();
		List<Department> departments =
			departmentRepository.findAllByCompany_IdAndDeletedAtIsNullOrderByCreatedAtAsc(companyId);
		Map<UUID, Long> memberCountMap = getMemberCountMap(companyId);

		Map<UUID, DepartmentTreeResponse> departmentMap = new LinkedHashMap<>();
		List<DepartmentTreeResponse> roots = new ArrayList<>();

		for (Department department : departments) {
			departmentMap.put(
				department.getId(),
				DepartmentTreeResponse.from(department, memberCountMap.getOrDefault(department.getId(), 0L))
			);
		}

		for (Department department : departments) {
			DepartmentTreeResponse response = departmentMap.get(department.getId());
			Department parentDepartment = department.getParentDepartment();

			if (parentDepartment == null) {
				roots.add(response);
				continue;
			}

			DepartmentTreeResponse parent = departmentMap.get(parentDepartment.getId());
			if (parent != null) {
				parent.addChild(response);
			}
		}

		return DepartmentListResponse.from(roots);
	}

	public DepartmentDetailResponse getDepartment(
		AuthenticatedMember authenticatedMember,
		UUID departmentId
	) {
		Member requester = validateSuperAdmin(authenticatedMember);
		Department department = findActiveDepartment(departmentId, requester.getCompany().getId());
		long memberCount = memberRepository.countByDepartment_IdAndDeletedAtIsNull(department.getId());

		return DepartmentDetailResponse.from(department, memberCount);
	}

	@Transactional
	public DepartmentCreateResponse createDepartment(
		AuthenticatedMember authenticatedMember,
		DepartmentCreateRequest request
	) {
		Member requester = validateSuperAdmin(authenticatedMember);
		Company company = requester.getCompany();
		Department parentDepartment = findParentDepartment(request.getParentDepartmentId(), company.getId());
		Member departmentManager = findDepartmentManager(request.getDepartmentManagerId(), company.getId());

		Department department = departmentRepository.save(Department.builder()
			.company(company)
			.parentDepartment(parentDepartment)
			.departmentManager(departmentManager)
			.name(request.getName())
			.build());

		return DepartmentCreateResponse.from(department);
	}

	@Transactional
	public DepartmentUpdateResponse updateDepartment(
		AuthenticatedMember authenticatedMember,
		UUID departmentId,
		DepartmentUpdateRequest request
	) {
		Member requester = validateSuperAdmin(authenticatedMember);
		Department department = findActiveDepartment(departmentId, requester.getCompany().getId());
		Member departmentManager = findDepartmentManager(
			request.getDepartmentManagerId(),
			requester.getCompany().getId()
		);

		department.update(request.getName(), departmentManager);

		return DepartmentUpdateResponse.from(department);
	}

	@Transactional
	public DepartmentDeleteResponse deleteDepartment(
		AuthenticatedMember authenticatedMember,
		UUID departmentId
	) {
		Member requester = validateSuperAdmin(authenticatedMember);
		Department department = findActiveDepartment(departmentId, requester.getCompany().getId());
		validateDeletable(department);
		LocalDateTime deletedAt = department.delete();

		return DepartmentDeleteResponse.from(department, deletedAt);
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

	private Department findParentDepartment(UUID parentDepartmentId, UUID companyId) {
		if (parentDepartmentId == null) {
			return null;
		}

		return findActiveDepartment(parentDepartmentId, companyId);
	}

	private Department findActiveDepartment(UUID departmentId, UUID companyId) {
		return departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(departmentId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
	}

	private Member findDepartmentManager(UUID departmentManagerId, UUID companyId) {
		if (departmentManagerId == null) {
			return null;
		}

		Member departmentManager = memberRepository.findById(departmentManagerId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!departmentManager.isActive() || !departmentManager.getCompany().getId().equals(companyId)) {
			throw new BusinessException(ErrorCode.INVALID_DEPARTMENT_MANAGER);
		}

		return departmentManager;
	}

	private void validateDeletable(Department department) {
		if (departmentRepository.existsByParentDepartment_IdAndDeletedAtIsNull(department.getId())) {
			throw new BusinessException(ErrorCode.DEPARTMENT_HAS_CHILDREN);
		}

		if (memberRepository.existsByDepartment_IdAndDeletedAtIsNull(department.getId())) {
			throw new BusinessException(ErrorCode.DEPARTMENT_HAS_MEMBERS);
		}
	}

	private Map<UUID, Long> getMemberCountMap(UUID companyId) {
		Map<UUID, Long> memberCountMap = new LinkedHashMap<>();

		for (Object[] row : departmentRepository.countMembersByDepartmentId(companyId)) {
			memberCountMap.put((UUID) row[0], (Long) row[1]);
		}

		return memberCountMap;
	}
}
