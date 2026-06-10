package com.ieumsae.assetieum.domain.intangibleasset.item.dto;

import com.ieumsae.assetieum.domain.intangibleasset.item.type.LicenseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntangibleAssetItemCreateRequest {
    @NotNull
    private UUID companyId;

    @NotNull
    private UUID categoryId;

    @NotBlank
    private String productName;

    @NotNull
    private String provider;

    @NotNull
    private LicenseType licenseType;

    @NotNull
    private Boolean isStandard;
}
