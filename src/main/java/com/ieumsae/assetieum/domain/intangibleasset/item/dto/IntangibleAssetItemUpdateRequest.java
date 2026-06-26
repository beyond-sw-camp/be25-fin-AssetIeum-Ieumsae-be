package com.ieumsae.assetieum.domain.intangibleasset.item.dto;


import com.ieumsae.assetieum.domain.intangibleasset.item.type.LicenseType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntangibleAssetItemUpdateRequest {
    private UUID categoryId;

    private String productName;

    private String provider;

    private LicenseType licenseType;

    private Boolean isStandard;
}
