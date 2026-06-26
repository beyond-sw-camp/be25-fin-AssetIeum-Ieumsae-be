package com.ieumsae.assetieum.domain.intangibleasset.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
        "itemId",
        "categoryId",
        "productName",
        "provider",
        "licenseType",
        "isStandard",
        "prePurchasePrice",
        "availableSeatCount",
        "createdAt",
        "updatedAt",
        "deletedAt"
})
public class IntangibleAssetItemResponse {

    private UUID itemId;

    private UUID categoryId;

    private String productName;

    private String provider;

    private String licenseType;

    private Boolean isStandard;

    private BigDecimal prePurchasePrice;

    private Integer availableSeatCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static IntangibleAssetItemResponse from(
            IntangibleAssetItem item
    ) {
        return from(item, 0);
    }

    public static IntangibleAssetItemResponse from(
            IntangibleAssetItem item,
            int availableSeatCount
    ) {
        return IntangibleAssetItemResponse.builder()
                .itemId(item.getId())
                .categoryId(
                        item.getIntangibleAssetCategory().getId()
                )
                .productName(item.getProductName())
                .provider(item.getProvider())
                .licenseType(
                        item.getLicenseType().name()
                )
                .isStandard(item.getIsStandard())
                .availableSeatCount(availableSeatCount)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public static IntangibleAssetItemResponse from(
            IntangibleAssetItem item,
            BigDecimal prePurchasePrice
    ) {
        return from(item, prePurchasePrice, 0);
    }

    public static IntangibleAssetItemResponse from(
            IntangibleAssetItem item,
            BigDecimal prePurchasePrice,
            int availableSeatCount
    ) {
        return IntangibleAssetItemResponse.builder()
                .itemId(item.getId())
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
                .availableSeatCount(availableSeatCount)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
