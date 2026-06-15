package com.ieumsae.assetieum.domain.inspection.inspection.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionTargetType;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectorType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InspectionResponse {

    private UUID inspectionId;

    private InspectionType inspectionType;

    private InspectionTargetType targetType;

    private UUID targetDepartmentId;

    private UUID targetCategoryId;

    private InspectorType inspectorType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    private InspectionStatus inspectionStatus;

    private String description;

    private UUID inspectorId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static InspectionResponse from(Inspection inspection) {
        return InspectionResponse.builder()
                .inspectionId(inspection.getId())
                .inspectionType(inspection.getInspectionType())
                .targetType(inspection.getTargetType())
                .targetDepartmentId(
                        inspection.getTargetDepartment() != null
                                ? inspection.getTargetDepartment().getId()
                                : null
                )
                .targetCategoryId(inspection.getTargetCategoryId())
                .inspectorType(inspection.getInspectorType())
                .startDate(inspection.getStartDate())
                .endDate(inspection.getEndDate())
                .inspectionStatus(inspection.getInspectionStatus())
                .description(inspection.getDescription())
                .inspectorId(inspection.getInspector().getId())
                .createdAt(inspection.getCreatedAt())
                .updatedAt(inspection.getUpdatedAt())
                .build();
    }
}
