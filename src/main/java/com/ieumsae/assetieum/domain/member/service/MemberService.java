package com.ieumsae.assetieum.domain.member.service;

import com.ieumsae.assetieum.global.common.util.KstDateTime;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.member.dto.MemberCreateRequest;
import com.ieumsae.assetieum.domain.member.dto.MemberCreateResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberDepartmentUpdateRequest;
import com.ieumsae.assetieum.domain.member.dto.MemberDepartmentUpdateResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberListItemResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberOffboardingCompleteResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberOffboardingStartRequest;
import com.ieumsae.assetieum.domain.member.dto.MemberOffboardingStartResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberOffboardingTargetsResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberSearchRequest;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
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

	private static final String ADMIN_DEPARTMENT_NAME = "관리자";
	private static final List<TicketStatus> ONGOING_TICKET_STATUSES = List.of(
		TicketStatus.REQUESTED,
		TicketStatus.DEPARTMENT_APPROVED,
		TicketStatus.ASSET_APPROVED,
		TicketStatus.IN_PROGRESS
	);

	private final MemberRepository memberRepository;
	private final DepartmentRepository departmentRepository;
	private final PasswordEncoder passwordEncoder;
	private final EntityManager entityManager;
	private final TicketRepository ticketRepository;

	public PaginationResponse<MemberListItemResponse> getMembers(
		AuthenticatedMember authenticatedMember,
		MemberSearchRequest request
	) {
		validateActiveMember(authenticatedMember);
		UUID companyId = authenticatedMember.companyId();
		validateSearchDepartment(request.getDepartmentId(), companyId);

		Page<MemberListItemResponse> members = memberRepository.searchMembers(
			companyId,
			normalizeKeyword(request.getKeyword()),
			request.getDepartmentId(),
			request.getStatus(),
			MemberRole.ADMIN,
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
		validateAssetManagerAssignable(companyId, request.getRole());

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
		validateDepartmentChangeAllowed(member);
		Department previousDepartment = member.getDepartment();
		Department currentDepartment = findActiveDepartment(request.getDepartmentId(), companyId);

		member.changeDepartment(currentDepartment);

		return MemberDepartmentUpdateResponse.from(member, previousDepartment);
	}

	public MemberOffboardingTargetsResponse getOffboardingTargets(
		AuthenticatedMember authenticatedMember,
		UUID memberId
	) {
		Member actor = validateOffboardingActor(authenticatedMember);
		Member targetMember = findMember(memberId, authenticatedMember.companyId());

		return buildOffboardingTargets(targetMember);
	}

	@Transactional
	public MemberOffboardingStartResponse startOffboarding(
		AuthenticatedMember authenticatedMember,
		UUID memberId,
		MemberOffboardingStartRequest request
	) {
		Member actor = validateOffboardingActor(authenticatedMember);
		Member targetMember = findMember(memberId, authenticatedMember.companyId());
		validateNotResigned(targetMember);

		LocalDateTime resignedAt = request.getResignedAt() == null ? KstDateTime.now() : request.getResignedAt();
		validateOffboardingCompletable(targetMember);
		targetMember.resign();

		return MemberOffboardingStartResponse.builder()
			.memberId(targetMember.getId())
			.memberName(targetMember.getName())
			.memberStatus(targetMember.getStatus())
			.returnedTangibleAssetCount(0)
			.endedIntangibleAssignmentCount(0)
			.remainingTargetCount(0)
			.resignedAt(resignedAt)
			.reason(request.getReason())
			.build();
	}

	@Transactional
	public MemberOffboardingCompleteResponse completeOffboarding(
		AuthenticatedMember authenticatedMember,
		UUID memberId
	) {
		Member actor = validateOffboardingActor(authenticatedMember);
		Member targetMember = findMember(memberId, authenticatedMember.companyId());
		validateNotResigned(targetMember);

		validateOffboardingCompletable(targetMember);

		targetMember.resign();
		return MemberOffboardingCompleteResponse.builder()
			.memberId(targetMember.getId())
			.memberName(targetMember.getName())
			.memberStatus(targetMember.getStatus())
			.completedAt(KstDateTime.now())
			.build();
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

	private Member validateOffboardingActor(AuthenticatedMember authenticatedMember) {
		Member actor = validateActiveMember(authenticatedMember);
		if (actor.getRole() != MemberRole.ADMIN) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		return actor;
	}

	private void validateNotResigned(Member member) {
		if (member.getStatus() == MemberStatus.RESIGNED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 퇴사 처리된 사원입니다.");
		}
	}

	private void validateOffboardingCompletable(Member member) {
		MemberOffboardingTargetsResponse targets = buildOffboardingTargets(member);
		if (targets.getRemainingTargetCount() > 0) {
			throw new BusinessException(
				ErrorCode.INVALID_INPUT_VALUE,
				"사용 중이거나 회수되지 않은 자산이 있어 퇴사 처리할 수 없습니다. 자산 반납/회수 처리를 먼저 완료해주세요."
			);
		}

		if (hasOngoingTickets(member)) {
			throw new BusinessException(
				ErrorCode.INVALID_INPUT_VALUE,
				"진행 중인 티켓이 있어 퇴사 처리할 수 없습니다. 티켓 처리를 먼저 완료해주세요."
			);
		}
	}

	private boolean hasOngoingTickets(Member member) {
		return ticketRepository.existsByCompany_IdAndRequester_IdAndTicketStatusInAndDeletedAtIsNull(
			member.getCompany().getId(),
			member.getId(),
			ONGOING_TICKET_STATUSES
		);
	}

	private Member findMember(UUID memberId, UUID companyId) {
		return memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
	}

	private MemberOffboardingTargetsResponse buildOffboardingTargets(Member member) {
		List<MemberOffboardingTargetsResponse.TangibleAssetTarget> tangibleAssets = findMemberTangibleAssets(member)
			.stream()
			.map(asset -> MemberOffboardingTargetsResponse.TangibleAssetTarget.builder()
				.assetId(asset.getId())
				.assetCode(asset.getAssetCode())
				.assetName(asset.getTangibleAssetItem().getProductName())
				.assetStatus(asset.getTangibleAssetStatus())
				.returnDueDate(asset.getReturnDueDate())
				.build())
			.toList();

		List<MemberOffboardingTargetsResponse.IntangibleAssetTarget> intangibleAssets = findMemberActiveIntangibleAssets(member)
			.stream()
			.map(row -> MemberOffboardingTargetsResponse.IntangibleAssetTarget.builder()
				.assetId((UUID) row[0])
				.assetCode((String) row[1])
				.assetName((String) row[2])
				.assetStatus((com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus) row[3])
				.expiredAt((LocalDateTime) row[4])
				.build())
			.toList();

		return MemberOffboardingTargetsResponse.builder()
			.memberId(member.getId())
			.memberName(member.getName())
			.departmentId(member.getDepartment().getId())
			.departmentName(member.getDepartment().getName())
			.memberStatus(member.getStatus())
			.tangibleAssets(tangibleAssets)
			.intangibleAssets(intangibleAssets)
			.remainingTargetCount(tangibleAssets.size() + intangibleAssets.size())
			.build();
	}

	private List<TangibleAsset> findMemberTangibleAssets(Member member) {
		return entityManager.createQuery("""
				select asset
				from TangibleAsset asset
				join fetch asset.tangibleAssetItem item
				where asset.company.id = :companyId
					and asset.member.id = :memberId
					and asset.tangibleAssetStatus <> :disposed
				order by asset.createdAt asc
				""", TangibleAsset.class)
			.setParameter("companyId", member.getCompany().getId())
			.setParameter("memberId", member.getId())
			.setParameter("disposed", TangibleAssetStatus.DISPOSED)
			.getResultList();
	}

	private List<Object[]> findMemberActiveIntangibleAssets(Member member) {
		return entityManager.createQuery("""
				select asset.id, asset.assetCode, item.productName, asset.intangibleAssetStatus, asset.expiredAt
				from IntangibleAssetAssignment assignment
				join assignment.intangibleAsset asset
				join asset.intangibleAssetItem item
				where assignment.company.id = :companyId
					and assignment.member.id = :memberId
					and assignment.assignmentStatus = :assignmentStatus
				order by assignment.assignedAt asc
				""", Object[].class)
			.setParameter("companyId", member.getCompany().getId())
			.setParameter("memberId", member.getId())
			.setParameter(
				"assignmentStatus",
				com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus.ACTIVE
			)
			.getResultList();
	}

	private long countMemberActiveIntangibleAssignments(Member member) {
		return findMemberActiveIntangibleAssets(member).size();
	}

	private Department findActiveDepartment(UUID departmentId, UUID companyId) {
		Department department = departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(departmentId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

		if (ADMIN_DEPARTMENT_NAME.equals(department.getName())) {
			throw new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND);
		}

		return department;
	}

	private Member findActiveMember(UUID memberId, UUID companyId) {
		Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		return member;
	}

	private void validateDepartmentChangeAllowed(Member member) {
		if (member.getRole() == MemberRole.DEPARTMENT_MANAGER
			|| member.getRole() == MemberRole.ASSET_MANAGER) {
			throw new BusinessException(ErrorCode.MEMBER_DEPARTMENT_CHANGE_NOT_ALLOWED);
		}
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
		if (!isManagerRole(role)) {
			return;
		}

		if (department.getDepartmentManager() != null) {
			throw new BusinessException(ErrorCode.INVALID_DEPARTMENT_MANAGER, "이미 부서장이 지정된 부서입니다.");
		}
	}

	private void validateAssetManagerAssignable(UUID companyId, MemberRole role) {
		if (role != MemberRole.ASSET_MANAGER) {
			return;
		}

		if (memberRepository.existsByCompany_IdAndRoleAndDeletedAtIsNull(companyId, MemberRole.ASSET_MANAGER)) {
			throw new BusinessException(ErrorCode.ASSET_MANAGER_ALREADY_EXISTS);
		}
	}

	private void assignDepartmentManagerIfNeeded(Department department, Member member) {
		if (!isManagerRole(member.getRole())) {
			return;
		}

		department.changeDepartmentManager(member);
	}

	private boolean isManagerRole(MemberRole role) {
		return role == MemberRole.DEPARTMENT_MANAGER || role == MemberRole.ASSET_MANAGER;
	}

	private String normalizeKeyword(String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return null;
		}

		return keyword.trim();
	}
}
