package com.ieumsae.assetieum.domain.inspection.result.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.inspection.result.entity.InspectionResult;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class InspectionResultResponse {

    private UUID inspectionResultId;

    private UUID inspectionId;

    private UUID inspectionTargetId;

    private String productName;

    private String assetCode;

    private Boolean followUpRequests;

    private String responseContent;

    private UUID reviewerId;

    private String reviewerName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static InspectionResultResponse from(InspectionResult inspectionResult) {
        InspectionTarget target = inspectionResult.getInspectionTarget();

        return InspectionResultResponse.builder()
                .inspectionResultId(inspectionResult.getId())
                .inspectionId(inspectionResult.getInspection().getId())
                .inspectionTargetId(target.getId())
                .productName(resolveProductName(target))
                .assetCode(resolveAssetCode(target))
                .followUpRequests(inspectionResult.getFollowUpRequests())
                .responseContent(inspectionResult.getResponseContent())
                .reviewerId(inspectionResult.getReviewer() == null ? null : inspectionResult.getReviewer().getId())
                .reviewerName(inspectionResult.getReviewer() == null ? null : inspectionResult.getReviewer().getName())
                .checkedAt(inspectionResult.getCheckedAt())
                .createdAt(inspectionResult.getCreatedAt())
                .updatedAt(inspectionResult.getUpdatedAt())
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
