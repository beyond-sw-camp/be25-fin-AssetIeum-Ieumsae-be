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

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "productName",
        "assetCode",
        "licenseCode",
        "status",
        "seatCount",
        "startedAt",
        "expiredAt",
        "isAutoRenewal",
        "billingCycle",
        "departmentName",
        "userName",
        "purchaseDate",
        "purchasePrice",
        "purchaseVendor"
})
public class IntangibleAssetDetailResponse {
    private String productName;

    private String assetCode;

    private String licenseCode;

    private IntangibleAssetStatus status;

    private Integer seatCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiredAt;

    private Boolean isAutoRenewal;

    private BillingCycle billingCycle;

    private String departmentName;

    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime purchaseDate;

    private BigDecimal purchasePrice;

    private String purchaseVendor;

    public static IntangibleAssetDetailResponse from(IntangibleAsset intangibleAsset) {
        return IntangibleAssetDetailResponse.builder()
                .productName(intangibleAsset.getIntangibleAssetItem().getProductName())
                .assetCode(intangibleAsset.getAssetCode())
                .licenseCode(intangibleAsset.getLicenseCode())
                .status(intangibleAsset.getIntangibleAssetStatus())
                .seatCount(intangibleAsset.getSeatCount())
                .startedAt(intangibleAsset.getStartedAt())
                .expiredAt(intangibleAsset.getExpiredAt())
                .isAutoRenewal(intangibleAsset.getIsAutoRenewal())
                .billingCycle(intangibleAsset.getBillingCycle())
                .departmentName(
                        intangibleAsset.getDepartment() != null
                                ? intangibleAsset.getDepartment().getName()
                                : null
                )
                .userName(
                        intangibleAsset.getMember() != null
                                ? intangibleAsset.getMember().getName()
                                : null
                )
                .purchaseDate(intangibleAsset.getPurchaseDate())
                .purchasePrice(intangibleAsset.getPurchasePrice())
                .purchaseVendor(intangibleAsset.getPurchaseVendor())
                .build();
    }
}
