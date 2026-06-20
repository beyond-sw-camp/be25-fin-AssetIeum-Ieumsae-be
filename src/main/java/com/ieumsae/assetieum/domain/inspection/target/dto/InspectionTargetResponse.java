package com.ieumsae.assetieum.domain.inspection.target.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InspectionTargetResponse {

    private UUID inspectionTargetId;

    private UUID inspectionId;

    private InspectionType inspectionType;

    private InspectionStatus inspectionStatus;

    private String productName;

    private String assetCode;

    private String category;

    private Boolean isResponded;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    public static InspectionTargetResponse from(InspectionTarget target) {
        return InspectionTargetResponse.builder()
                .inspectionTargetId(target.getId())
                .inspectionId(target.getInspection().getId())
                .inspectionType(target.getInspection().getInspectionType())
                .inspectionStatus(target.getInspection().getInspectionStatus())
                .productName(resolveProductName(target))
                .assetCode(resolveAssetCode(target))
                .category(resolveCategory(target))
                .isResponded(target.getIsResponded())
                .startDate(target.getInspection().getStartDate())
                .endDate(target.getInspection().getEndDate())
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

    private static String resolveCategory(InspectionTarget target) {
        if (target.getTangibleAsset() != null) {
            return target.getTangibleAsset().getTangibleAssetItem().getTangibleAssetCategory().getName();
        }

        if (target.getIntangibleAsset() != null) {
            return target.getIntangibleAsset().getIntangibleAssetItem().getIntangibleAssetCategory().getName();
        }

        return null;
    }
}
