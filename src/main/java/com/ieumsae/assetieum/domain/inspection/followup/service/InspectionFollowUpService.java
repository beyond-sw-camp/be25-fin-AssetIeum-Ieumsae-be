package com.ieumsae.assetieum.domain.inspection.followup.service;

import com.ieumsae.assetieum.domain.inspection.followup.dto.InspectionFollowUpResponse;
import com.ieumsae.assetieum.domain.inspection.followup.dto.InspectionFollowUpStatusRequest;
import com.ieumsae.assetieum.domain.inspection.followup.entity.InspectionFollowUp;
import com.ieumsae.assetieum.domain.inspection.followup.repository.InspectionFollowUpRepository;
import com.ieumsae.assetieum.domain.inspection.followup.type.InspectionFollowUpStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.service.InspectionService;
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
public class InspectionFollowUpService {

    private final InspectionFollowUpRepository inspectionFollowUpRepository;
    private final InspectionService inspectionService;

    public InspectionFollowUpResponse getInspectionFollowUp(
            UUID followUpId,
            AuthenticatedMember member
    ) {
        return InspectionFollowUpResponse.from(findFollowUp(followUpId, member.companyId()));
    }

    @Transactional
    public InspectionFollowUpResponse updateInspectionFollowUpStatus(
            UUID followUpId,
            InspectionFollowUpStatusRequest request,
            AuthenticatedMember member
    ) {
        InspectionFollowUp followUp = findFollowUp(followUpId, member.companyId());
        InspectionFollowUpStatus nextStatus = request.getStatus();

        if (followUp.getInspectionFollowUpStatus() != nextStatus) {
            validateStatusTransition(followUp.getInspectionFollowUpStatus(), nextStatus);
        }

        LocalDateTime processedAt = nextStatus == InspectionFollowUpStatus.COMPLETED
                ? LocalDateTime.now()
                : null;

        followUp.updateStatus(nextStatus, request.getActionDetail(), processedAt);
        if (nextStatus == InspectionFollowUpStatus.COMPLETED) {
            inspectionService.closeIfCompletedAndAllFollowUpsCompleted(
                    followUp.getInspectionResult().getInspection()
            );
        }

        return InspectionFollowUpResponse.from(followUp);
    }

    private InspectionFollowUp findFollowUp(UUID followUpId, UUID companyId) {
        InspectionFollowUp followUp = inspectionFollowUpRepository.findById(followUpId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_FOLLOW_UP_NOT_FOUND));

        if (!followUp.getCompany().getId().equals(companyId)) {
            throw new BusinessException(ErrorCode.INSPECTION_FOLLOW_UP_NOT_FOUND);
        }

        return followUp;
    }

    private void validateStatusTransition(
            InspectionFollowUpStatus currentStatus,
            InspectionFollowUpStatus nextStatus
    ) {
        if (currentStatus == InspectionFollowUpStatus.PENDING
                && nextStatus == InspectionFollowUpStatus.IN_PROGRESS) {
            return;
        }

        if (currentStatus == InspectionFollowUpStatus.IN_PROGRESS
                && nextStatus == InspectionFollowUpStatus.COMPLETED) {
            return;
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }
}
