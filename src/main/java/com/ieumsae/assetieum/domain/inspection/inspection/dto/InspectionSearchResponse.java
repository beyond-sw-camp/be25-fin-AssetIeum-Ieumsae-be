package com.ieumsae.assetieum.domain.inspection.inspection.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectorType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class InspectionSearchResponse {

    private UUID inspectionId;

    private InspectionType inspectionType;

    private String targetName;

    private UUID inspectorId;

    private String inspectorName;

    private InspectorType inspectorType;

    private InspectionStatus inspectionStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    public static InspectionSearchResponse from(Inspection inspection, String targetName) {
        return InspectionSearchResponse.builder()
                .inspectionId(inspection.getId())
                .inspectionType(inspection.getInspectionType())
                .targetName(targetName)
                .inspectorId(inspection.getInspector().getId())
                .inspectorName(inspection.getInspector().getName())
                .inspectorType(inspection.getInspectorType())
                .inspectionStatus(inspection.getInspectionStatus())
                .startDate(inspection.getStartDate())
                .endDate(inspection.getEndDate())
                .build();
    }
}
