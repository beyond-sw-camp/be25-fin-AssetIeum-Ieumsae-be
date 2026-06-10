package com.ieumsae.assetieum.domain.tangibleasset.asset.dto;

import com.ieumsae.assetieum.domain.tangibleasset.asset.type.AssetUsageType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TangibleAssetCreateRequest {
    @NotNull
    private UUID companyId;

    @NotNull
    private UUID tangibleItemId;

    @NotBlank
    private String serialNumber;

    @NotNull
    private UsageType usageType;

    @NotNull
    private AssetUsageType assetUsageType;

    @NotBlank
    private String location;

    @NotNull
    private LocalDateTime purchaseDate;

    @NotNull
    private BigDecimal purchasePrice;

    @NotBlank
    private String purchaseVendor;

    @NotNull
    private LocalDateTime warrantyExpiredAt;
}