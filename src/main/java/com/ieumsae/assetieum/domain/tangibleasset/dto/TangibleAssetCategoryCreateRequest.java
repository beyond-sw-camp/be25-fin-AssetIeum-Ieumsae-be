package com.ieumsae.assetieum.domain.tangibleasset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TangibleAssetCategoryCreateRequest {
    @NotNull
    private UUID companyId;

    private UUID parentId;

    @NotBlank
    private String name;
}
