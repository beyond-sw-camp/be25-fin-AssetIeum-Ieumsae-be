package com.ieumsae.assetieum.domain.inspection.followup.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.inspection.followup.entity.InspectionFollowUp;
import com.ieumsae.assetieum.domain.inspection.followup.type.InspectionFollowUpStatus;
import com.ieumsae.assetieum.domain.inspection.result.entity.InspectionResult;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class InspectionFollowUpResponse {

    private UUID followUpId;

    private UUID inspectionResultId;

    private UUID inspectionId;

    private UUID inspectionTargetId;

    private String productName;

    private String assetCode;

    private String responseContent;

    private String actionDetail;

    private UUID processorId;

    private String processorName;

    private InspectionFollowUpStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static InspectionFollowUpResponse from(InspectionFollowUp followUp) {
        InspectionResult result = followUp.getInspectionResult();
        InspectionTarget target = result.getInspectionTarget();

        return InspectionFollowUpResponse.builder()
                .followUpId(followUp.getId())
                .inspectionResultId(result.getId())
                .inspectionId(result.getInspection().getId())
                .inspectionTargetId(target.getId())
                .productName(resolveProductName(target))
                .assetCode(resolveAssetCode(target))
                .responseContent(result.getResponseContent())
                .actionDetail(followUp.getActionDetail())
                .processorId(followUp.getProcessor() == null ? null : followUp.getProcessor().getId())
                .processorName(followUp.getProcessor() == null ? null : followUp.getProcessor().getName())
                .status(followUp.getInspectionFollowUpStatus())
                .processedAt(followUp.getProcessedAt())
                .createdAt(followUp.getCreatedAt())
                .updatedAt(followUp.getUpdatedAt())
                .build();
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
