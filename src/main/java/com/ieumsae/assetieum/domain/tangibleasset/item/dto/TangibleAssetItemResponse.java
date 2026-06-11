package com.ieumsae.assetieum.domain.tangibleasset.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({
        "itemId",
        "companyId",
        "categoryId",
        "productName",
        "manufacturer",
        "modelName",
        "isStandard",
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static TangibleAssetItemResponse from(TangibleAssetItem item) {
        return TangibleAssetItemResponse.builder()
                .itemId(item.getId())
                .categoryId(item.getTangibleAssetCategory().getId())
                .productName(item.getProductName())
                .manufacturer(item.getManufacturer())
                .modelName(item.getModelName())
                .isStandard(item.getIsStandard())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}