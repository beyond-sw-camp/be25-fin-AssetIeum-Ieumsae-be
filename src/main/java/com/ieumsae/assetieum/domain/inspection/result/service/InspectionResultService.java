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

import java.time.LocalDateTime;
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
    public InspectionResultResponse createTangibleAssetInspectionResult(
            UUID targetId,
            InspectionResultCreateRequest request,
            AuthenticatedMember authenticatedMember
    ) {
        // 1. 입력값 검증
        UUID companyId = authenticatedMember.companyId();

        InspectionTarget target = inspectionTargetRepository.findByIdAndCompany_Id(targetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_TARGET_NOT_FOUND));
        Inspection inspection = target.getInspection();

        Member reviewer = memberRepository
                .findByIdAndCompany_IdAndDeletedAtIsNull(authenticatedMember.id(), companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        validateResultCreatable(inspection, target, reviewer, companyId);

        // 2. 전수조사 응답 등록
        InspectionResult inspectionResult = inspectionResultRepository.save(InspectionResult.builder()
                .company(reviewer.getCompany())
                .inspection(inspection)
                .inspectionTarget(target)
                .followUpRequests(request.getFollowUpRequests())
                .responseContent(request.getResponseContent().trim())
                .reviewer(reviewer)
                .checkedAt(LocalDateTime.now())
                .build());

        // 3. 해당 대상 자산 응답 여부 기록
        target.markResponded();

        // 4. 필요 시, 후속 처리 필요 여부 등록
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

    private void validateResultCreatable(
            Inspection inspection,
            InspectionTarget target,
            Member reviewer,
            UUID companyId
    ) {
        if (inspection.getInspectionType() != InspectionType.TANGIBLE_ASSET) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (inspection.getInspectionStatus() != InspectionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (Boolean.TRUE.equals(target.getIsResponded())
                || inspectionResultRepository.existsByInspectionTarget_IdAndCompany_Id(target.getId(), companyId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        validateReviewer(inspection, target, reviewer);
    }

    private void validateReviewer(Inspection inspection, InspectionTarget target, Member reviewer) {
        if (inspection.getInspectorType() == InspectorType.ASSET_TEAM) {
            if (isAssetTeamReviewer(reviewer)) {
                return;
            }
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (inspection.getInspectionType() == InspectionType.TANGIBLE_ASSET) {
            validateAssignedMember(target.getTangibleAsset().getMember(), reviewer);
            return;
        }

        validateAssignedMember(target.getIntangibleAsset().getMember(), reviewer);
    }

    private boolean isAssetTeamReviewer(Member reviewer) {
        return reviewer.getRole() == MemberRole.ASSET_MANAGER
                || reviewer.getRole() == MemberRole.ASSET_TEAM
                || reviewer.getRole() == MemberRole.ADMIN;
    }

    private void validateAssignedMember(Member assignedMember, Member reviewer) {
        if (assignedMember != null && assignedMember.getId().equals(reviewer.getId())) {
            return;
        }

        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
}
