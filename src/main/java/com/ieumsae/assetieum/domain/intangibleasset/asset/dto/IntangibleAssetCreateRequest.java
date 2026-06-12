package com.ieumsae.assetieum.domain.intangibleasset.asset.dto;

import com.ieumsae.assetieum.domain.intangibleasset.asset.type.BillingCycle;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
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
public class IntangibleAssetCreateRequest {

    @NotNull
    private UUID intangibleItemId;

    private String licenseCode;

    @NotNull
    private Integer seatCount;

    @NotNull
    private Boolean isAutoRenewal;

    @NotNull
    private LocalDateTime purchaseDate;

    @NotNull
    private BigDecimal purchasePrice;

    @NotBlank
    private String purchaseVendor;

    private IntangibleAssetStatus intangibleAssetStatus;

    private UUID memberId;

    private UUID departmentId;

    private LocalDateTime startedAt;

    private LocalDateTime expiredAt;

    private BillingCycle billingCycle;

}
