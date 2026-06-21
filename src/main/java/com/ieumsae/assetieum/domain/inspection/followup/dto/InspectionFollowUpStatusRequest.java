package com.ieumsae.assetieum.domain.inspection.followup.dto;

import com.ieumsae.assetieum.domain.inspection.followup.type.InspectionFollowUpStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InspectionFollowUpStatusRequest {

    @NotNull
    private InspectionFollowUpStatus status;

    private String actionDetail;

}
