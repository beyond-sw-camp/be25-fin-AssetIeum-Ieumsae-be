package com.ieumsae.assetieum.domain.hr.hrevent.service.handler;

import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.entity.HrEventAssetTarget;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.type.HrEventAssetActionType;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.service.IntangibleAssetAssignmentService;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.service.TangibleAssetAssignmentService;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.ticket.assetreturn.dto.AssetReturnTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.assetreturn.service.AssetReturnTicketService;
import com.ieumsae.assetieum.domain.ticket.assetreturn.type.AssetReturnTargetType;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HrEventAssetTargetProcessor {

    private static final String HR_EVENT_AUTO_REQUEST_REASON = "HR 이벤트 자동 생성";

    private final TangibleAssetAssignmentService tangibleAssetAssignmentService;
    private final IntangibleAssetAssignmentService intangibleAssetAssignmentService;
    private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
    private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
    private final AssetReturnTicketService assetReturnTicketService;

    public void process(HrEvent hrEvent, HrEventAssetTarget target, UUID companyId, boolean allowKeep) {
        HrEventAssetActionType actionType = target.getActionType();

        if (actionType == HrEventAssetActionType.KEEP) {
            if (!allowKeep) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            transferDepartment(hrEvent, target, companyId);
            target.process(LocalDateTime.now());
            return;
        }

        if (actionType == HrEventAssetActionType.TRANSFER_REQUIRED) {
            transferMember(target, companyId);
            target.process(LocalDateTime.now());
            return;
        }

        if (actionType == HrEventAssetActionType.RETURN_REQUIRED
                || actionType == HrEventAssetActionType.UNASSIGN_REQUIRED) {
            if (isAlreadyReturnedOrUnassigned(target, companyId)) {
                target.complete(LocalDateTime.now());
                return;
            }
            createReturnTicket(target, companyId);
            target.process(LocalDateTime.now());
            return;
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private void transferDepartment(HrEvent hrEvent, HrEventAssetTarget target, UUID companyId) {
        if (hrEvent.getTargetDepartment() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (target.getAssetType() == AssetType.TANGIBLE) {
            tangibleAssetAssignmentService.transferDepartment(
                    target.getTangibleAsset().getId(),
                    hrEvent.getMember().getId(),
                    hrEvent.getTargetDepartment(),
                    companyId
            );
            return;
        }

        if (target.getAssetType() == AssetType.INTANGIBLE) {
            intangibleAssetAssignmentService.transferDepartment(
                    target.getIntangibleAsset().getId(),
                    hrEvent.getMember().getId(),
                    hrEvent.getTargetDepartment(),
                    companyId
            );
            return;
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private void transferMember(HrEventAssetTarget target, UUID companyId) {
        if (target.getTransferMember() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (target.getAssetType() == AssetType.TANGIBLE) {
            tangibleAssetAssignmentService.reassignAsset(
                    target.getTangibleAsset().getId(),
                    target.getTransferMember().getId(),
                    companyId
            );
            return;
        }

        if (target.getAssetType() == AssetType.INTANGIBLE) {
            intangibleAssetAssignmentService.reassignAsset(
                    target.getIntangibleAsset().getId(),
                    target.getMember().getId(),
                    target.getTransferMember().getId(),
                    companyId
            );
            return;
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private void createReturnTicket(HrEventAssetTarget target, UUID companyId) {
        AssetReturnTicketCreateRequest request = new AssetReturnTicketCreateRequest();
        request.setAssetType(resolveReturnTargetType(target));
        request.setAssignmentId(resolveAssignmentId(target, companyId));
        request.setRequestReason(HR_EVENT_AUTO_REQUEST_REASON);

        assetReturnTicketService.createAssetReturnTicket(toAuthenticatedMember(target.getMember()), request);
    }

    private AssetReturnTargetType resolveReturnTargetType(HrEventAssetTarget target) {
        if (target.getAssetType() == AssetType.TANGIBLE) {
            return AssetReturnTargetType.TANGIBLE;
        }

        if (target.getAssetType() == AssetType.INTANGIBLE) {
            return AssetReturnTargetType.INTANGIBLE;
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private boolean isAlreadyReturnedOrUnassigned(HrEventAssetTarget target, UUID companyId) {
        if (target.getAssetType() == AssetType.TANGIBLE) {
            TangibleAssetStatus status = target.getTangibleAsset().getTangibleAssetStatus();
            if (status != TangibleAssetStatus.IN_USE) {
                return true;
            }

            return tangibleAssetAssignmentRepository
                    .findByCompany_IdAndTangibleAsset_IdAndAssignmentStatus(
                            companyId,
                            target.getTangibleAsset().getId(),
                            com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus.ACTIVE
                    )
                    .isEmpty();
        }

        if (target.getAssetType() == AssetType.INTANGIBLE) {
            if (target.getIntangibleAsset().getIntangibleAssetStatus() != com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus.IN_USE) {
                return true;
            }

            if (target.getIntangibleAssetAssignment() != null) {
                return target.getIntangibleAssetAssignment().getAssignmentStatus() != com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus.ACTIVE;
            }

            return intangibleAssetAssignmentRepository
                    .findByCompany_IdAndIntangibleAsset_IdAndMember_IdAndAssignmentStatus(
                            companyId,
                            target.getIntangibleAsset().getId(),
                            target.getMember().getId(),
                            AssignmentStatus.ACTIVE
                    )
                    .isEmpty();
        }

        return false;
    }

    private UUID resolveAssignmentId(HrEventAssetTarget target, UUID companyId) {
        if (target.getAssetType() == AssetType.TANGIBLE) {
            TangibleAssetAssignment assignment = tangibleAssetAssignmentRepository
                    .findByCompany_IdAndTangibleAsset_IdAndAssignmentStatus(
                            companyId,
                            target.getTangibleAsset().getId(),
                            com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus.ACTIVE
                    )
                    .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ASSIGNMENT_NOT_FOUND));
            return assignment.getId();
        }

        if (target.getAssetType() == AssetType.INTANGIBLE) {
            if (target.getIntangibleAssetAssignment() != null) {
                return target.getIntangibleAssetAssignment().getId();
            }

            return intangibleAssetAssignmentRepository
                    .findByCompany_IdAndIntangibleAsset_IdAndMember_IdAndAssignmentStatus(
                            companyId,
                            target.getIntangibleAsset().getId(),
                            target.getMember().getId(),
                            com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus.ACTIVE
                    )
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ASSIGNMENT_NOT_FOUND))
                    .getId();
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private AuthenticatedMember toAuthenticatedMember(Member member) {
        return new AuthenticatedMember(
                member.getId(),
                member.getCompany().getId(),
                member.getMemberNo(),
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
    }
}
