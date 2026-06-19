package com.ieumsae.assetieum.domain.purchase.purchaseplan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ieumsae.assetieum.domain.intangibleasset.item.type.LicenseType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchasePlanItemCreateItemRequest {

    @NotNull
    private UUID categoryId;

    @Size(max = 100)
    private String manufacturer;

    @Size(max = 255)
    private String modelName;

    @Size(max = 100)
    private String provider;

    private LicenseType licenseType;

    @NotNull
    private Boolean isStandard;
}
