package com.ieumsae.assetieum.domain.hr.hrtemplate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.hr.hrtemplate.entity.HrTemplate;
import com.ieumsae.assetieum.domain.hr.hrtemplateitem.dto.HrTemplateItemResponse;
import com.ieumsae.assetieum.domain.hr.hrtemplateitem.entity.HrTemplateItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
        "hrTemplateId",
        "departmentId",
        "departmentName",
        "items",
        "createdAt",
        "updatedAt",
        "deletedAt"
})
public class HrTemplateResponse {

    private UUID hrTemplateId;

    private UUID departmentId;

    private String departmentName;

    private List<HrTemplateItemResponse> items;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;

    public static HrTemplateResponse from(HrTemplate hrTemplate) {
        return from(hrTemplate, List.of());
    }

    public static HrTemplateResponse from(HrTemplate hrTemplate, List<HrTemplateItem> items) {
        Department department = hrTemplate.getDepartment();

        return HrTemplateResponse.builder()
                .hrTemplateId(hrTemplate.getId())
                .departmentId(department.getId())
                .departmentName(department.getName())
                .items(items.stream()
                        .map(HrTemplateItemResponse::from)
                        .toList())
                .createdAt(hrTemplate.getCreatedAt())
                .updatedAt(hrTemplate.getUpdatedAt())
                .deletedAt(hrTemplate.getDeletedAt())
                .build();
    }
}
