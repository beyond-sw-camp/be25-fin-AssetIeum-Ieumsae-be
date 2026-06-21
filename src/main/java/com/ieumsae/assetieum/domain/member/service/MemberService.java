package com.ieumsae.assetieum.domain.member.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import com.ieumsae.assetieum.domain.hr.hrevent.repository.HrEventRepository;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventStatus;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
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
import com.ieumsae.assetieum.domain.intangibleasset.assignment.service.IntangibleAssetAssignmentService;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.service.TangibleAssetAssignmentService;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketService;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

	private final MemberRepository memberRepository;
	private final DepartmentRepository departmentRepository;
	private final PasswordEncoder passwordEncoder;
	private final EntityManager entityManager;
	private final HrEventRepository hrEventRepository;
	private final TangibleAssetAssignmentService tangibleAssetAssignmentService;
	private final IntangibleAssetAssignmentService intangibleAssetAssignmentService;
	private final TicketService ticketService;

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

		LocalDateTime resignedAt = request.getResignedAt() == null ? LocalDateTime.now() : request.getResignedAt();
		List<TangibleAsset> tangibleAssets = findMemberTangibleAssets(targetMember);
		List<Object[]> intangibleAssets = findMemberActiveIntangibleAssets(targetMember);
		List<Ticket> activeTickets = findMemberActiveTickets(targetMember);
		Optional<HrEvent> offboardingEvent = findOpenOffboardingEvent(targetMember);
		offboardingEvent
			.filter(event -> event.getHrEventStatus() == HrEventStatus.PENDING)
			.ifPresent(HrEvent::start);

		long returnedTangibleAssetCount = returnTangibleAssets(targetMember, tangibleAssets);
		long endedIntangibleAssignmentCount = endIntangibleAssignments(targetMember, intangibleAssets);
		long cancelledTicketCount = cancelActiveTickets(authenticatedMember, activeTickets);
		long remainingTargetCount = buildOffboardingTargets(targetMember).getRemainingTargetCount();

		return MemberOffboardingStartResponse.builder()
			.memberId(targetMember.getId())
			.memberName(targetMember.getName())
			.memberStatus(targetMember.getStatus())
			.returnedTangibleAssetCount(returnedTangibleAssetCount)
			.endedIntangibleAssignmentCount(endedIntangibleAssignmentCount)
			.cancelledTicketCount(cancelledTicketCount)
			.remainingTargetCount(remainingTargetCount)
			.resignedAt(resignedAt)
			.reason(request.getReason())
			.hrEventId(offboardingEvent.map(HrEvent::getId).orElse(null))
			.hrEventStatus(offboardingEvent.map(HrEvent::getHrEventStatus).orElse(null))
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

		MemberOffboardingTargetsResponse targets = buildOffboardingTargets(targetMember);
		if (targets.getRemainingTargetCount() > 0) {
			throw new BusinessException(
				ErrorCode.INVALID_INPUT_VALUE,
				"회수되지 않은 자산 또는 진행 중인 티켓이 있어 퇴사 완료 처리할 수 없습니다."
			);
		}

		targetMember.resign();
		Optional<HrEvent> offboardingEvent = findOpenOffboardingEvent(targetMember);
		offboardingEvent.ifPresent(HrEvent::complete);
		return MemberOffboardingCompleteResponse.builder()
			.memberId(targetMember.getId())
			.memberName(targetMember.getName())
			.memberStatus(targetMember.getStatus())
			.completedAt(LocalDateTime.now())
			.hrEventId(offboardingEvent.map(HrEvent::getId).orElse(null))
			.hrEventStatus(offboardingEvent.map(HrEvent::getHrEventStatus).orElse(null))
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

	private Member findMember(UUID memberId, UUID companyId) {
		return memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
	}

	private Optional<HrEvent> findOpenOffboardingEvent(Member member) {
		return hrEventRepository
			.findAllByCompany_IdAndMember_IdAndEventTypeAndHrEventStatusInAndCancelledAtIsNullOrderByEventDateDesc(
				member.getCompany().getId(),
				member.getId(),
				HrEventType.OFFBOARDING,
				List.of(HrEventStatus.PENDING, HrEventStatus.IN_PROGRESS)
			)
			.stream()
			.findFirst();
	}

	private long returnTangibleAssets(Member targetMember, List<TangibleAsset> tangibleAssets) {
		long count = 0;
		for (TangibleAsset asset : tangibleAssets) {
			if (asset.getTangibleAssetStatus() != TangibleAssetStatus.IN_USE) {
				continue;
			}
			tangibleAssetAssignmentService.cancelAsset(asset.getId(), targetMember.getCompany().getId());
			count++;
		}
		return count;
	}

	private long endIntangibleAssignments(Member targetMember, List<Object[]> intangibleAssets) {
		long count = 0;
		for (Object[] row : intangibleAssets) {
			UUID assetId = (UUID) row[0];
			intangibleAssetAssignmentService.cancelAsset(
				assetId,
				targetMember.getId(),
				targetMember.getCompany().getId()
			);
			count++;
		}
		return count;
	}

	private long cancelActiveTickets(AuthenticatedMember authenticatedMember, List<Ticket> activeTickets) {
		long count = 0;
		for (Ticket ticket : activeTickets) {
			if (ticket.getTicketStatus() == TicketStatus.CANCELLED
				|| ticket.getTicketStatus() == TicketStatus.COMPLETED) {
				continue;
			}
			ticketService.cancelTicketForOffboarding(authenticatedMember.companyId(), ticket.getId());
			count++;
		}
		return count;
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

		List<MemberOffboardingTargetsResponse.ActiveTicketTarget> activeTickets = findMemberActiveTickets(member)
			.stream()
			.map(ticket -> MemberOffboardingTargetsResponse.ActiveTicketTarget.builder()
				.ticketId(ticket.getId())
				.ticketNo(ticket.getTicketNo())
				.ticketType(ticket.getTicketType())
				.ticketStatus(ticket.getTicketStatus())
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
			.activeTickets(activeTickets)
			.remainingTargetCount(tangibleAssets.size() + intangibleAssets.size() + activeTickets.size())
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

	private List<Ticket> findMemberActiveTickets(Member member) {
		return entityManager.createQuery("""
				select ticket
				from Ticket ticket
				where ticket.company.id = :companyId
					and ticket.requester.id = :memberId
					and ticket.ticketStatus in :statuses
					and ticket.deletedAt is null
				order by ticket.createdAt asc
				""", Ticket.class)
			.setParameter("companyId", member.getCompany().getId())
			.setParameter("memberId", member.getId())
			.setParameter("statuses", List.of(
				TicketStatus.REQUESTED,
				TicketStatus.DEPARTMENT_APPROVED,
				TicketStatus.ASSET_APPROVED,
				TicketStatus.IN_PROGRESS
			))
			.getResultList();
	}

	private long countMemberActiveIntangibleAssignments(Member member) {
		return findMemberActiveIntangibleAssets(member).size();
	}

	private long countMemberActiveTickets(Member member) {
		return findMemberActiveTickets(member).size();
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
