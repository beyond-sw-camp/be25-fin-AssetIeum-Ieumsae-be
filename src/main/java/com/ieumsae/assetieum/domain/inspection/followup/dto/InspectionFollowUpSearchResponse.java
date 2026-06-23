package com.ieumsae.assetieum.domain.inspection.followup.dto;

import com.ieumsae.assetieum.domain.inspection.followup.entity.InspectionFollowUp;
import com.ieumsae.assetieum.domain.inspection.followup.type.InspectionFollowUpStatus;
import com.ieumsae.assetieum.domain.inspection.result.entity.InspectionResult;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import com.ieumsae.assetieum.domain.member.entity.Member;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InspectionFollowUpSearchResponse {

    private UUID inspectionFollowUpId;

    private UUID inspectorId;

    private String inspectorName;

    private UUID memberId;

    private String memberName;

    private String productName;

    private String assetCode;

    private String responseContent;

    private String actionDetail;

    private InspectionFollowUpStatus status;

    public static InspectionFollowUpSearchResponse from(InspectionFollowUp followUp) {
        InspectionResult result = followUp.getInspectionResult();
        InspectionTarget target = result.getInspectionTarget();
        Member member = resolveMember(target);

        return InspectionFollowUpSearchResponse.builder()
                .inspectionFollowUpId(followUp.getId())
                .inspectorId(result.getInspection().getInspector().getId())
                .inspectorName(result.getInspection().getInspector().getName())
                .memberId(member == null ? null : member.getId())
                .memberName(member == null ? null : member.getName())
                .productName(resolveProductName(target))
                .assetCode(resolveAssetCode(target))
                .responseContent(result.getResponseContent())
                .actionDetail(followUp.getActionDetail())
                .status(followUp.getInspectionFollowUpStatus())
                .build();
    }

    private static Member resolveMember(InspectionTarget target) {
        if (target.getMember() != null) {
            return target.getMember();
        }

        if (target.getTangibleAsset() != null) {
            return target.getTangibleAsset().getMember();
        }

        return null;
    }

    private static String resolveProductName(InspectionTarget target) {
        if (target.getTangibleAsset() != null) {
            return target.getTangibleAsset().getTangibleAssetItem().getProductName();
        }

        if (target.getIntangibleAsset() != null) {
            return target.getIntangibleAsset().getIntangibleAssetItem().getProductName();
        }

        return null;
    }

    private static String resolveAssetCode(InspectionTarget target) {
        if (target.getTangibleAsset() != null) {
            return target.getTangibleAsset().getAssetCode();
        }

        if (target.getIntangibleAsset() != null) {
            return target.getIntangibleAsset().getAssetCode();
        }

        return null;
    }
}
