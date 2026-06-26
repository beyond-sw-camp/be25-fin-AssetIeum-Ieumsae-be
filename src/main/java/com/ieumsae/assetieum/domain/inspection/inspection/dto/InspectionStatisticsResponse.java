package com.ieumsae.assetieum.domain.inspection.inspection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionStatisticsResponse {

    private Long totalInspectionCount;

    private Long readyInspectionCount;

    private Long inProgressInspectionCount;

    private Long completedInspectionCount;

    private Long inProgressTargetAssetCount;

    private Long completedTargetAssetCount;

    private Long unprocessedAssetCount;

    private Long followUpInProgressAssetCount;

    private Long followUpCompletedAssetCount;
}
