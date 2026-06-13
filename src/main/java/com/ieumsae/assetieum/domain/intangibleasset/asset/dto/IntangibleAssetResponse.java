package com.ieumsae.assetieum.domain.intangibleasset.asset.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.BillingCycle;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "intangibleAssetId",
        "intangibleItemId",
        "assetCode",
        "licenseCode",
        "intangibleAssetStatus",
        "memberId",
        "departmentId",
        "seatCount",
        "startedAt",
        "expiredAt",
        "isAutoRenewal",
        "billingCycle",
        "purchaseDate",
        "purchasePrice",
        "purchaseVendor",
        "createdAt",
        "updatedAt"
})
public class IntangibleAssetResponse {
    private UUID intangibleAssetId;
    private UUID intangibleItemId;

    private String assetCode;
    private String licenseCode;
    private IntangibleAssetStatus intangibleAssetStatus;

    private UUID memberId;
    private UUID departmentId;

    private Integer seatCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiredAt;

    private Boolean isAutoRenewal;

    private BillingCycle billingCycle;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseDate;
    private BigDecimal purchasePrice;
    private String purchaseVendor;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static IntangibleAssetResponse from(IntangibleAsset intangibleAsset) {
        return IntangibleAssetResponse.builder()
                .intangibleAssetId(intangibleAsset.getId())
                .intangibleItemId(intangibleAsset.getIntangibleAssetItem().getId())
                .assetCode(intangibleAsset.getAssetCode())
                .licenseCode(intangibleAsset.getLicenseCode())
                .intangibleAssetStatus(intangibleAsset.getIntangibleAssetStatus())
                .memberId(
                        intangibleAsset.getMember() != null
                                ? intangibleAsset.getMember().getId()
                                : null
                )
                .departmentId(
                        intangibleAsset.getDepartment() != null
                                ? intangibleAsset.getDepartment().getId()
                                : null
                )
                .seatCount(intangibleAsset.getSeatCount())
                .startedAt(intangibleAsset.getStartedAt())
                .expiredAt(intangibleAsset.getExpiredAt())
                .isAutoRenewal(intangibleAsset.getIsAutoRenewal())
                .billingCycle(intangibleAsset.getBillingCycle())
                .purchaseDate(intangibleAsset.getPurchaseDate())
                .purchasePrice(intangibleAsset.getPurchasePrice())
                .purchaseVendor(intangibleAsset.getPurchaseVendor())
                .createdAt(intangibleAsset.getCreatedAt())
                .updatedAt(intangibleAsset.getUpdatedAt())
                .build();
    }
}
