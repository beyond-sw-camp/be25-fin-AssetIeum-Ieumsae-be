package com.ieumsae.assetieum.domain.tangibleasset.asset.dto;

import com.ieumsae.assetieum.domain.tangibleasset.asset.type.AssetUsageType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
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
    private UUID tangibleItemId;

    @NotBlank
    private String serialNumber;

    @NotNull
    private UsageType usageType;

    @NotNull
    private AssetUsageType assetUsageType;

    private TangibleAssetStatus tangibleAssetStatus;

    private UUID memberId;

    private UUID departmentId;

    @NotBlank
    private String location;

    private LocalDateTime usedStartedAt;

    private LocalDateTime returnDueDate;

    @NotNull
    private LocalDateTime purchaseDate;

    @NotNull
    private BigDecimal purchasePrice;

    @NotBlank
    private String purchaseVendor;

    @NotNull
    private LocalDateTime warrantyExpiredAt;
}
