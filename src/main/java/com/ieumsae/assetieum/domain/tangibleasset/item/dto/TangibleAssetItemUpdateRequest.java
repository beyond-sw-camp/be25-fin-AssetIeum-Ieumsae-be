package com.ieumsae.assetieum.domain.tangibleasset.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TangibleAssetItemUpdateRequest {
    private UUID categoryId;

    private String productName;

    private String manufacturer;

    private String modelName;

    private Boolean isStandard;
}
