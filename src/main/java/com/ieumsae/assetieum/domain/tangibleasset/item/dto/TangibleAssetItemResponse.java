package com.ieumsae.assetieum.domain.tangibleasset.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({
        "itemId",
        "categoryId",
        "productName",
        "manufacturer",
        "modelName",
        "isStandard",
        "prePurchasePrice",
        "availableAssetCount",
        "createdAt",
        "updatedAt",
        "deletedAt"
})
public class TangibleAssetItemResponse {
    private UUID itemId;

    private UUID categoryId;

    private String productName;

    private String manufacturer;

    private String modelName;

    private Boolean isStandard;

    private BigDecimal prePurchasePrice;

    private Integer availableAssetCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static TangibleAssetItemResponse from(TangibleAssetItem item) {
        return from(item, 0);
    }

    public static TangibleAssetItemResponse from(TangibleAssetItem item, int availableAssetCount) {
        return TangibleAssetItemResponse.builder()
                .itemId(item.getId())
                .categoryId(item.getTangibleAssetCategory().getId())
                .productName(item.getProductName())
                .manufacturer(item.getManufacturer())
                .modelName(item.getModelName())
                .isStandard(item.getIsStandard())
                .availableAssetCount(availableAssetCount)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public static TangibleAssetItemResponse from(TangibleAssetItem item, BigDecimal prePurchasePrice) {
        return from(item, prePurchasePrice, 0);
    }

    public static TangibleAssetItemResponse from(
            TangibleAssetItem item,
            BigDecimal prePurchasePrice,
            int availableAssetCount
    ) {
        return TangibleAssetItemResponse.builder()
                .itemId(item.getId())
                .categoryId(item.getTangibleAssetCategory().getId())
                .productName(item.getProductName())
                .manufacturer(item.getManufacturer())
                .modelName(item.getModelName())
                .isStandard(item.getIsStandard())
                .prePurchasePrice(prePurchasePrice)
                .availableAssetCount(availableAssetCount)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
