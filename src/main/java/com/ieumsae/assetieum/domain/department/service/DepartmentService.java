package com.ieumsae.assetieum.domain.department.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
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
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

	private static final String ADMIN_DEPARTMENT_NAME = "관리자";

	private final DepartmentRepository departmentRepository;
	private final MemberRepository memberRepository;
	private final CompanyRepository companyRepository;

	public DepartmentListResponse getDepartments(AuthenticatedMember authenticatedMember) {
		validateActiveMember(authenticatedMember);
		UUID companyId = authenticatedMember.companyId();
		List<Department> departments =
			departmentRepository.findAllByCompany_IdAndNameNotAndDeletedAtIsNullOrderByCreatedAtAsc(
				companyId,
				ADMIN_DEPARTMENT_NAME
			);
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
		validateAdmin(authenticatedMember);
		Department department = findActiveDepartment(departmentId, authenticatedMember.companyId());
		long memberCount = memberRepository.countByDepartment_IdAndDeletedAtIsNull(department.getId());

		return DepartmentDetailResponse.from(department, memberCount);
	}

	@Transactional
	public DepartmentCreateResponse createDepartment(
		AuthenticatedMember authenticatedMember,
		DepartmentCreateRequest request
	) {
		validateAdmin(authenticatedMember);
		UUID companyId = authenticatedMember.companyId();
		Company company = findActiveCompany(companyId);
		Department parentDepartment = findParentDepartment(request.getParentDepartmentId(), companyId);
		Member departmentManager = findDepartmentManager(request.getDepartmentManagerId(), companyId);
		validateDepartmentManagerAssignable(departmentManager, null);
		assignDepartmentManagerRoleIfNeeded(departmentManager);

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
		validateAdmin(authenticatedMember);
		UUID companyId = authenticatedMember.companyId();
		Department department = findActiveDepartment(departmentId, companyId);
		Department parentDepartment = department.getParentDepartment();
		String name = department.getName();
		Member previousDepartmentManager = department.getDepartmentManager();
		Member departmentManager = department.getDepartmentManager();

		if (request.parentDepartmentIdProvided()) {
			parentDepartment = findParentDepartment(
				request.getParentDepartmentId(),
				companyId
			);
			validateParentDepartment(department, parentDepartment);
		}

		if (request.nameProvided()) {
			validateDepartmentName(request.getName());
			name = request.getName();
		}

		if (request.departmentManagerIdProvided()) {
			departmentManager = findDepartmentManager(
				request.getDepartmentManagerId(),
				companyId
			);
			validateDepartmentManagerAssignable(departmentManager, previousDepartmentManager);
			changeDepartmentManagerRole(previousDepartmentManager, departmentManager);
		}

		department.update(parentDepartment, name, departmentManager);

		return DepartmentUpdateResponse.from(department);
	}

	@Transactional
	public DepartmentDeleteResponse deleteDepartment(
		AuthenticatedMember authenticatedMember,
		UUID departmentId
	) {
		validateAdmin(authenticatedMember);
		Department department = findActiveDepartment(departmentId, authenticatedMember.companyId());
		validateDeletable(department);
		LocalDateTime deletedAt = department.delete();

		return DepartmentDeleteResponse.from(department, deletedAt);
	}

	private void validateAdmin(AuthenticatedMember authenticatedMember) {
		Member member = validateActiveMember(authenticatedMember);

		if (member.getRole() != MemberRole.ADMIN) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private Member validateActiveMember(AuthenticatedMember authenticatedMember) {
		Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
				authenticatedMember.id(),
				authenticatedMember.companyId()
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		return member;
	}

	private Company findActiveCompany(UUID companyId) {
		return companyRepository.findByIdAndDeletedAtIsNull(companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
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

	private void validateDepartmentManagerAssignable(
		Member departmentManager,
		Member currentDepartmentManager
	) {
		if (departmentManager == null) {
			return;
		}

		if (currentDepartmentManager != null
			&& currentDepartmentManager.getId().equals(departmentManager.getId())) {
			return;
		}

		if (!isStaffRole(departmentManager.getRole())) {
			throw new BusinessException(ErrorCode.INVALID_DEPARTMENT_MANAGER);
		}
	}

	private void assignDepartmentManagerRoleIfNeeded(Member departmentManager) {
		if (departmentManager == null || isManagerRole(departmentManager.getRole())) {
			return;
		}

		if (departmentManager.getRole() == MemberRole.ASSET_TEAM) {
			departmentManager.changeRole(MemberRole.ASSET_MANAGER);
			return;
		}

		departmentManager.changeRole(MemberRole.DEPARTMENT_MANAGER);
	}

	private void changeDepartmentManagerRole(Member previousDepartmentManager, Member currentDepartmentManager) {
		if (previousDepartmentManager != null
			&& (currentDepartmentManager == null
			|| !previousDepartmentManager.getId().equals(currentDepartmentManager.getId()))) {
			demoteDepartmentManager(previousDepartmentManager);
		}

		assignDepartmentManagerRoleIfNeeded(currentDepartmentManager);
	}

	private void demoteDepartmentManager(Member departmentManager) {
		if (departmentManager.getRole() == MemberRole.ASSET_MANAGER) {
			departmentManager.changeRole(MemberRole.ASSET_TEAM);
			return;
		}

		departmentManager.changeRole(MemberRole.EMPLOYEE);
	}

	private boolean isStaffRole(MemberRole role) {
		return role == MemberRole.EMPLOYEE || role == MemberRole.ASSET_TEAM;
	}

	private boolean isManagerRole(MemberRole role) {
		return role == MemberRole.DEPARTMENT_MANAGER || role == MemberRole.ASSET_MANAGER;
	}

	private void validateParentDepartment(Department department, Department parentDepartment) {
		if (parentDepartment == null) {
			return;
		}

		Department current = parentDepartment;
		while (current != null) {
			if (current.getId().equals(department.getId())) {
				throw new BusinessException(ErrorCode.INVALID_PARENT_DEPARTMENT);
			}

			current = current.getParentDepartment();
		}
	}

	private void validateDepartmentName(String name) {
		if (!StringUtils.hasText(name)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
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

		for (Object[] row : departmentRepository.countMembersByDepartmentId(companyId, MemberRole.ADMIN)) {
			memberCountMap.put((UUID) row[0], (Long) row[1]);
		}

		return memberCountMap;
	}
}
