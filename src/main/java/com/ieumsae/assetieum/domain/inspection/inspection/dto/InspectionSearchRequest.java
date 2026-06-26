package com.ieumsae.assetieum.domain.inspection.inspection.dto;

import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class InspectionSearchRequest extends PaginationRequest {

    private InspectionStatus status;

    private UUID inspectorId;

}
