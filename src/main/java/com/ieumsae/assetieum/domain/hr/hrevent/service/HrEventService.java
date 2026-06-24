package com.ieumsae.assetieum.domain.hr.hrevent.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventCreateRequest;
import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventResponse;
import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventSearchRequest;
import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import com.ieumsae.assetieum.domain.hr.hrevent.repository.HrEventRepository;
import com.ieumsae.assetieum.domain.hr.hrevent.service.handler.HrEventHandlerResolver;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventStatus;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.dto.HrEventAssetTargetCreateRequest;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.dto.HrEventAssetTargetResponse;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.entity.HrEventAssetTarget;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.repository.HrEventAssetTargetRepository;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.type.HrEventAssetActionType;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HrEventService {

    private static final String HR_EVENT_NO_PREFIX = "EVT";
    private static final String REDIS_KEY_PREFIX = "hr-event:no:";

    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final HrEventRepository hrEventRepository;
    private final HrEventAssetTargetRepository hrEventAssetTargetRepository;
    private final TangibleAssetRepository tangibleAssetRepository;
    private final IntangibleAssetRepository intangibleAssetRepository;
    private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
    private final HrEventHandlerResolver hrEventHandlerResolver;
    private final CodeGenerator codeGenerator;

    @Transactional
    public HrEventResponse createHrEvent(
            HrEventCreateRequest request,
            AuthenticatedMember member
    ) {
        Company company = companyRepository.findById(member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member authenticatedMember = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
                        member.id(),
                        member.companyId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Member targetMember = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
                        request.getMemberId(),
                        member.companyId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        validateSameDepartment(authenticatedMember, targetMember.getDepartment().getId());
        Department targetDepartment = resolveTargetDepartment(request, targetMember, member.companyId());

        Department department = departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
                        authenticatedMember.getDepartment().getId(),
                        member.companyId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        HrEvent hrEvent = HrEvent.builder()
                .company(company)
                .department(department)
                .targetDepartment(targetDepartment)
                .member(targetMember)
                .hrEventNo(codeGenerator.generate(HR_EVENT_NO_PREFIX, REDIS_KEY_PREFIX, member.companyId()))
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .build();

        HrEvent savedHrEvent = hrEventRepository.save(hrEvent);
        List<HrEventAssetTarget> assetTargets = createHrEventAssetTargets(
                request.getAssetTargets(),
                savedHrEvent,
                company,
                targetMember,
                member.companyId()
        );
        hrEventAssetTargetRepository.saveAll(assetTargets);

        return HrEventResponse.from(savedHrEvent);
    }

    private Department resolveTargetDepartment(
            HrEventCreateRequest request,
            Member targetMember,
            UUID companyId
    ) {
        if (request.getEventType() != HrEventType.DEPARTMENT_TRANSFER) {
            if (request.getTargetDepartmentId() != null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            return null;
        }

        if (request.getTargetDepartmentId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Department targetDepartment = departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
                        request.getTargetDepartmentId(),
                        companyId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        if (targetDepartment.getId().equals(targetMember.getDepartment().getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return targetDepartment;
    }

    private List<HrEventAssetTarget> createHrEventAssetTargets(
            List<HrEventAssetTargetCreateRequest> requests,
            HrEvent hrEvent,
            Company company,
            Member targetMember,
            UUID companyId
    ) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        return requests.stream()
                .map(request -> createHrEventAssetTarget(request, hrEvent, company, targetMember, companyId))
                .toList();
    }

    private HrEventAssetTarget createHrEventAssetTarget(
            HrEventAssetTargetCreateRequest request,
            HrEvent hrEvent,
            Company company,
            Member targetMember,
            UUID companyId
    ) {
        validateActionType(hrEvent.getEventType(), request);

        TangibleAsset tangibleAsset = null;
        IntangibleAsset intangibleAsset = null;
        IntangibleAssetAssignment intangibleAssetAssignment = null;
        Member transferMember = resolveTransferMember(request, targetMember, companyId);

        if (request.getAssetType() == AssetType.TANGIBLE) {
            tangibleAsset = tangibleAssetRepository.findByIdAndCompany_Id(request.getAssetId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));
            validateTargetMember(tangibleAsset.getMember(), targetMember);
        } else if (request.getAssetType() == AssetType.INTANGIBLE) {
            intangibleAsset = intangibleAssetRepository.findByIdAndCompany_Id(request.getAssetId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_FOUND));
            intangibleAssetAssignment = resolveActiveIntangibleAssignment(companyId, intangibleAsset, targetMember);
        } else {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return HrEventAssetTarget.builder()
                .company(company)
                .hrEvent(hrEvent)
                .member(targetMember)
                .assetType(request.getAssetType())
                .tangibleAsset(tangibleAsset)
                .intangibleAsset(intangibleAsset)
                .intangibleAssetAssignment(intangibleAssetAssignment)
                .transferMember(transferMember)
                .actionType(request.getActionType())
                .build();
    }

    private IntangibleAssetAssignment resolveActiveIntangibleAssignment(
            UUID companyId,
            IntangibleAsset intangibleAsset,
            Member targetMember
    ) {
        IntangibleAssetAssignment assignment = intangibleAssetAssignmentRepository
                .findByCompany_IdAndIntangibleAsset_IdAndMember_IdAndAssignmentStatus(
                        companyId,
                        intangibleAsset.getId(),
                        targetMember.getId(),
                        AssignmentStatus.ACTIVE
                )
                .orElse(null);

        if (assignment == null) {
            validateTargetMember(intangibleAsset.getMember(), targetMember);
        }

        return assignment;
    }

    private void validateActionType(HrEventType eventType, HrEventAssetTargetCreateRequest request) {
        if (eventType == HrEventType.OFFBOARDING
                && request.getActionType() == HrEventAssetActionType.KEEP) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (request.getActionType() == HrEventAssetActionType.TRANSFER_REQUIRED
                && request.getTransferMemberId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (request.getActionType() != HrEventAssetActionType.TRANSFER_REQUIRED
                && request.getTransferMemberId() != null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Member resolveTransferMember(
            HrEventAssetTargetCreateRequest request,
            Member targetMember,
            UUID companyId
    ) {
        if (request.getActionType() != HrEventAssetActionType.TRANSFER_REQUIRED) {
            return null;
        }

        Member transferMember = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
                        request.getTransferMemberId(),
                        companyId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (transferMember.getId().equals(targetMember.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!transferMember.getDepartment().getId().equals(targetMember.getDepartment().getId())) {
            throw new BusinessException(ErrorCode.HR_EVENT_MEMBER_DEPARTMENT_MISMATCH);
        }

        return transferMember;
    }

    private void validateTargetMember(Member assetMember, Member targetMember) {
        if (assetMember == null || !assetMember.getId().equals(targetMember.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateSameDepartment(Member authenticatedMember, UUID departmentId) {
        if (!authenticatedMember.getDepartment().getId().equals(departmentId)) {
            throw new BusinessException(ErrorCode.HR_EVENT_MEMBER_DEPARTMENT_MISMATCH);
        }
    }

    @Transactional
    public HrEventResponse deleteHrEvent(
            UUID eventId,
            AuthenticatedMember member
    ) {
        companyRepository.findById(member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member authenticatedMember = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
                        member.id(),
                        member.companyId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        HrEvent hrEvent = hrEventRepository.findByIdAndCompany_IdAndCancelledAtIsNull(eventId, member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HR_EVENT_NOT_FOUND));

        validateSameDepartment(authenticatedMember, hrEvent.getDepartment().getId());

        if (hrEvent.getHrEventStatus() != HrEventStatus.PENDING) {
            throw new BusinessException(ErrorCode.HR_EVENT_ALREADY_IN_PROGRESS);
        }

        hrEvent.delete();

        List<HrEventAssetTarget> hrEventAssetTargets =
                hrEventAssetTargetRepository.findAllByHrEvent_IdAndCompany_Id(hrEvent.getId(), member.companyId());

        for (HrEventAssetTarget target : hrEventAssetTargets) {
            target.cancel();
        }

        return HrEventResponse.from(hrEvent);
    }

    public PaginationResponse<HrEventResponse> getHrEvents(
            HrEventSearchRequest request,
            AuthenticatedMember authenticatedMember
    ) {
        companyRepository.findById(authenticatedMember.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
                        authenticatedMember.id(),
                        authenticatedMember.companyId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Page<HrEventResponse> eventPage = hrEventRepository.search(
                member.getCompany().getId(),
                member.getDepartment().getId(),
                request.getHrEventStatus(),
                request.getHrEventType(),
                request.toPageable()
        );

        return PaginationResponse.from(eventPage);
    }

    public List<HrEventAssetTargetResponse> getHrEventAssetTargets(
            UUID eventId,
            AuthenticatedMember member
    ) {
        hrEventRepository.findByIdAndCompany_IdAndCancelledAtIsNull(eventId, member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HR_EVENT_NOT_FOUND));

        return hrEventAssetTargetRepository.findAllByHrEvent_IdAndCompany_IdOrderByCreatedAtAsc(
                        eventId,
                        member.companyId()
                )
                .stream()
                .map(HrEventAssetTargetResponse::from)
                .toList();
    }

    @Transactional
    public HrEventResponse completeHrEvent(
            UUID eventId,
            AuthenticatedMember member
    ) {
        companyRepository.findById(member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        HrEvent hrEvent = hrEventRepository.findByIdAndCompany_IdAndCancelledAtIsNull(eventId, member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HR_EVENT_NOT_FOUND));

        if (hrEvent.getHrEventStatus() != HrEventStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.HR_EVENT_NOT_IN_PROGRESS);
        }

        completeRelatedTargets(hrEvent);
        hrEvent.complete();

        return HrEventResponse.from(hrEvent);
    }

    @Transactional
    public void executeDueHrEvents(LocalDate executionDate) {
        LocalDateTime startInclusive = executionDate.atStartOfDay();
        LocalDateTime endExclusive = startInclusive.plusDays(1);

        List<HrEvent> hrEvents = hrEventRepository
                .findAllByHrEventStatusAndEventDateGreaterThanEqualAndEventDateLessThanAndCancelledAtIsNullOrderByEventDateAsc(
                        HrEventStatus.PENDING,
                        startInclusive,
                        endExclusive
                );

        hrEvents.forEach(this::executeHrEvent);
    }

    @Transactional
    public void completeDueOffboardingHrEvents(LocalDate executionDate) {
        LocalDateTime startInclusive = executionDate.atStartOfDay();
        LocalDateTime endExclusive = startInclusive.plusDays(1);

        List<HrEvent> hrEvents = hrEventRepository
                .findAllByHrEventStatusAndEventTypeAndEventDateGreaterThanEqualAndEventDateLessThanAndCancelledAtIsNullOrderByEventDateAsc(
                        HrEventStatus.IN_PROGRESS,
                        HrEventType.OFFBOARDING,
                        startInclusive,
                        endExclusive
                );

        hrEvents.forEach(this::completeOffboardingHrEvent);
    }

    private void executeHrEvent(HrEvent hrEvent) {
        hrEventHandlerResolver.resolve(hrEvent.getEventType()).handle(hrEvent);
    }

    private void completeRelatedTargets(HrEvent hrEvent) {
        List<HrEventAssetTarget> hrEventAssetTargets =
                hrEventAssetTargetRepository.findAllByHrEvent_IdAndCompany_Id(hrEvent.getId(), hrEvent.getCompany().getId());

        LocalDateTime completedAt = LocalDateTime.now();
        for (HrEventAssetTarget target : hrEventAssetTargets) {
            target.complete(completedAt);
        }
    }

    private void completeOffboardingHrEvent(HrEvent hrEvent) {
        hrEvent.getMember().resign();
        completeRelatedTargets(hrEvent);
        hrEvent.complete();
    }
}
