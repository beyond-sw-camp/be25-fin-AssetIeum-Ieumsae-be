package com.ieumsae.assetieum.domain.inspection.inspection.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectorType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonPropertyOrder({
        "inspectionInfo",
        "inspectionResults",
        "uninspectedAssets"
})
public class InspectionDetailResponse {

    private InspectionInfo inspectionInfo;

    private List<InspectionResultItem> inspectionResults;

    private List<UninspectedAssetItem> uninspectedAssets;

    public static InspectionDetailResponse of(
            Inspection inspection,
            String targetName,
            List<InspectionResultItem> inspectionResults,
            List<UninspectedAssetItem> uninspectedAssets
    ) {
        return InspectionDetailResponse.builder()
                .inspectionInfo(InspectionInfo.from(inspection, targetName))
                .inspectionResults(inspectionResults)
                .uninspectedAssets(uninspectedAssets)
                .build();
    }

    @Getter
    @Builder
    public static class InspectionInfo {

        private String inspectorName;

        private String targetName;

        private InspectorType inspectorType;

        private InspectionStatus inspectionStatus;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startDate;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endDate;

        private static InspectionInfo from(Inspection inspection, String targetName) {
            return InspectionInfo.builder()
                    .inspectorName(inspection.getInspector().getName())
                    .targetName(targetName)
                    .inspectorType(inspection.getInspectorType())
                    .inspectionStatus(inspection.getInspectionStatus())
                    .startDate(inspection.getStartDate())
                    .endDate(inspection.getEndDate())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class InspectionResultItem {

        private String productName;

        private String assetCode;

        private boolean followUpRequired;

        private String userResponseContent;
    }

    @Getter
    @Builder
    public static class UninspectedAssetItem {

        private String productName;

        private String assetCode;

        private String category;
    }
}
