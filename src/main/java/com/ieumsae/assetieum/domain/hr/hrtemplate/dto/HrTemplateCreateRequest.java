package com.ieumsae.assetieum.domain.hr.hrtemplate.dto;

import com.ieumsae.assetieum.domain.hr.hrtemplateitem.dto.HrTemplateItemCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HrTemplateCreateRequest {

    @Valid
    @NotEmpty(message = "HR 템플릿 품목은 1개 이상이어야 합니다.")
    private List<HrTemplateItemCreateRequest> items;

}
