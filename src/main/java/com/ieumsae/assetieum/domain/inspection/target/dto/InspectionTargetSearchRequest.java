package com.ieumsae.assetieum.domain.inspection.target.dto;

import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InspectionTargetSearchRequest extends PaginationRequest {

    private InspectionStatus status;

    private Boolean isResponded;
}
