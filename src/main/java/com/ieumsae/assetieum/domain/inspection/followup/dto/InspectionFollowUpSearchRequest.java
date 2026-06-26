package com.ieumsae.assetieum.domain.inspection.followup.dto;

import com.ieumsae.assetieum.domain.inspection.followup.type.InspectionFollowUpStatus;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InspectionFollowUpSearchRequest extends PaginationRequest {

    private InspectionFollowUpStatus status;

    private String keyword;

}
