package com.ieumsae.assetieum.domain.tangibleasset.category.dto;

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

    private UUID parentId;

    @NotBlank
    private String name;
}
