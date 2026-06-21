package com.ieumsae.assetieum.domain.inspection.result.service;

import com.ieumsae.assetieum.domain.inspection.followup.entity.InspectionFollowUp;
import com.ieumsae.assetieum.domain.inspection.followup.repository.InspectionFollowUpRepository;
import com.ieumsae.assetieum.domain.inspection.followup.type.InspectionFollowUpStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectorType;
import com.ieumsae.assetieum.domain.inspection.result.dto.InspectionResultCreateRequest;
import com.ieumsae.assetieum.domain.inspection.result.dto.InspectionResultResponse;
import com.ieumsae.assetieum.domain.inspection.result.entity.InspectionResult;
import com.ieumsae.assetieum.domain.inspection.result.repository.InspectionResultRepository;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import com.ieumsae.assetieum.domain.inspection.target.repository.InspectionTargetRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InspectionResultService {

    private final InspectionTargetRepository inspectionTargetRepository;
    private final InspectionResultRepository inspectionResultRepository;
    private final InspectionFollowUpRepository inspectionFollowUpRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public InspectionResultResponse createInspectionResult(
            UUID targetId,
            InspectionResultCreateRequest request,
            AuthenticatedMember authenticatedMember
    ) {
        UUID companyId = authenticatedMember.companyId();
        InspectionTarget target = findTarget(targetId, companyId);
        Inspection inspection = target.getInspection();
        Member reviewer = findActiveMember(authenticatedMember.id(), companyId);

        validateResultCreatable(inspection, target, reviewer, companyId);

        InspectionResult inspectionResult = inspectionResultRepository.save(InspectionResult.builder()
                .company(reviewer.getCompany())
                .inspection(inspection)
                .inspectionTarget(target)
                .followUpRequests(request.getFollowUpRequests())
                .responseContent(request.getResponseContent().trim())
                .reviewer(reviewer)
                .build());

        target.markResponded();

        if (Boolean.TRUE.equals(request.getFollowUpRequests())) {
            inspectionFollowUpRepository.save(InspectionFollowUp.builder()
                    .company(reviewer.getCompany())
                    .inspectionResult(inspectionResult)
                    .processor(inspection.getInspector())
                    .inspectionFollowUpStatus(InspectionFollowUpStatus.PENDING)
                    .build());
        }

        return InspectionResultResponse.from(inspectionResult);
    }

    public InspectionResultResponse getInspectionResult(
            UUID targetId,
            AuthenticatedMember authenticatedMember
    ) {
        UUID companyId = authenticatedMember.companyId();
        InspectionTarget target = findTarget(targetId, companyId);
        Member viewer = findActiveMember(authenticatedMember.id(), companyId);

        validateResultReadable(target.getInspection(), target, viewer);

        InspectionResult inspectionResult = inspectionResultRepository
                .findByInspectionTarget_IdAndCompany_Id(targetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_RESULT_NOT_FOUND));

        return InspectionResultResponse.from(inspectionResult);
    }

    private InspectionTarget findTarget(UUID targetId, UUID companyId) {
        return inspectionTargetRepository.findByIdAndCompany_Id(targetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_TARGET_NOT_FOUND));
    }

    private Member findActiveMember(UUID memberId, UUID companyId) {
        Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
        }

        return member;
    }

    private void validateResultCreatable(
            Inspection inspection,
            InspectionTarget target,
            Member reviewer,
            UUID companyId
    ) {
        if (inspection.getInspectionStatus() != InspectionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (Boolean.TRUE.equals(target.getIsResponded())
                || inspectionResultRepository.existsByInspectionTarget_IdAndCompany_Id(target.getId(), companyId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        validateResultWriter(inspection, target, reviewer);
    }

    private void validateResultWriter(Inspection inspection, InspectionTarget target, Member reviewer) {
        if (inspection.getInspectorType() == InspectorType.ASSET_TEAM) {
            if (isAssetManagerRole(reviewer)) {
                return;
            }
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        validateAssignedMember(resolveAssignedMember(inspection, target), reviewer);
    }

    private void validateResultReadable(Inspection inspection, InspectionTarget target, Member viewer) {
        if (isAssetManagerRole(viewer)) {
            return;
        }

        if (inspection.getInspectorType() == InspectorType.EMPLOYEE) {
            validateAssignedMember(resolveAssignedMember(inspection, target), viewer);
            return;
        }

        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    private boolean isAssetManagerRole(Member member) {
        return member.getRole() == MemberRole.ASSET_MANAGER
                || member.getRole() == MemberRole.ASSET_TEAM
                || member.getRole() == MemberRole.ADMIN;
    }

    private Member resolveAssignedMember(Inspection inspection, InspectionTarget target) {
        if (inspection.getInspectionType() == InspectionType.TANGIBLE_ASSET) {
            return target.getTangibleAsset() == null ? null : target.getTangibleAsset().getMember();
        }

        return target.getIntangibleAsset() == null ? null : target.getIntangibleAsset().getMember();
    }

    private void validateAssignedMember(Member assignedMember, Member member) {
        if (assignedMember != null && assignedMember.getId().equals(member.getId())) {
            return;
        }

        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
}
