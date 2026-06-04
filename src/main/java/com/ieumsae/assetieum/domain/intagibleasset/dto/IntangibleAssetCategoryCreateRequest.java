package com.ieumsae.assetieum.domain.intagibleasset.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntangibleAssetCategoryCreateRequest {
    @NotBlank
    private UUID companyId;

    private UUID parentId;

    @NotBlank
    private String name;
}
