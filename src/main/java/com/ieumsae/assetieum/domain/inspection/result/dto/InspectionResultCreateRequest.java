package com.ieumsae.assetieum.domain.inspection.result.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InspectionResultCreateRequest {

    @NotNull
    private Boolean followUpRequests;

    @NotBlank
    private String responseContent;

}
