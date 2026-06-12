package com.ieumsae.assetieum.domain.intangibleasset.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({
        "itemId",
        "companyId",
        "categoryId",
        "productName",
        "provider",
        "licenseType",
        "isStandard",
        "prePurchasePrice",
        "createdAt",
        "updatedAt",
        "deletedAt"
})
public class IntangibleAssetItemResponse {

    private UUID itemId;

    private UUID companyId;

    private UUID categoryId;

    private String productName;

    private String provider;

    private String licenseType;

    private Boolean isStandard;

    private BigDecimal prePurchasePrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static IntangibleAssetItemResponse from(
            IntangibleAssetItem item
    ) {
        return IntangibleAssetItemResponse.builder()
                .itemId(item.getId())
                .companyId(item.getCompany().getId())
                .categoryId(
                        item.getIntangibleAssetCategory().getId()
                )
                .productName(item.getProductName())
                .provider(item.getProvider())
                .licenseType(
                        item.getLicenseType().name()
                )
                .isStandard(item.getIsStandard())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public static IntangibleAssetItemResponse from(
            IntangibleAssetItem item,
            BigDecimal prePurchasePrice
    ) {
        return IntangibleAssetItemResponse.builder()
                .itemId(item.getId())
                .companyId(item.getCompany().getId())
                .categoryId(
                        item.getIntangibleAssetCategory().getId()
                )
                .productName(item.getProductName())
                .provider(item.getProvider())
                .licenseType(
                        item.getLicenseType().name()
                )
                .isStandard(item.getIsStandard())
                .prePurchasePrice(prePurchasePrice)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
