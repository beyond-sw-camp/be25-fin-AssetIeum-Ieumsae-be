package com.ieumsae.assetieum.domain.purchase.purchaseplanitem.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "assetType",
    "category",
    "productName",
    "quantity",
    "estimatedUnitPrice",
    "totalAmount"
})
public class PurchasePlanItemResponse {

    private AssetType assetType;

    private String category;

    private String productName;

    private Integer quantity;

    private BigDecimal estimatedUnitPrice;

    private BigDecimal totalAmount;

    public static PurchasePlanItemResponse from(PurchasePlanItem item) {
        return PurchasePlanItemResponse.builder()
                .assetType(item.getAssetType())
                .category(resolveCategory(item))
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .estimatedUnitPrice(item.getEstimatedUnitPrice())
                .totalAmount(item.getEstimatedUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    private static String resolveCategory(PurchasePlanItem item) {
        if (item.getTangibleAssetItem() != null) {
            return item.getTangibleAssetItem().getTangibleAssetCategory().getName();
        }

        if (item.getIntangibleAssetItem() != null) {
            return item.getIntangibleAssetItem().getIntangibleAssetCategory().getName();
        }

        return null;
    }
}
