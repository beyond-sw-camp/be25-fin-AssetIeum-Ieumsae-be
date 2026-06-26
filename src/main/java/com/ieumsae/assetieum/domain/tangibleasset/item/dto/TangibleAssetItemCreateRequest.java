package com.ieumsae.assetieum.domain.tangibleasset.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TangibleAssetItemCreateRequest {

    @NotNull
    private UUID categoryId;

    @NotBlank
    private String productName;

    @NotBlank
    private String manufacturer;

    @NotBlank
    private String modelName;

    @NotNull
    private Boolean isStandard;

}
