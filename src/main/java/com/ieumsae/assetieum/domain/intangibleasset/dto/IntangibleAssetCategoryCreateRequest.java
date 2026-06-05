package com.ieumsae.assetieum.domain.intangibleasset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntangibleAssetCategoryCreateRequest {
    @NotNull
    private UUID companyId;

    private UUID parentId;

    @NotBlank
    private String name;
}
