package com.ieumsae.assetieum.domain.inspection.inspection.dto;

import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionTargetType;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectorType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InspectionCreateRequest {
    @NotNull
    private InspectionTargetType targetType;

    private UUID targetDepartmentId;

    private UUID targetCategoryId;

    @NotNull
    private InspectorType inspectorType;

    String description;

    @NotNull
    UUID inspectorId;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;
}
