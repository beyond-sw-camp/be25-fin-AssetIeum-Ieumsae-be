package com.ieumsae.assetieum.domain.purchase.purchaseplanitem.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "category",
    "itemName",
    "quantity",
    "estimatedUnitPrice",
    "totalAmount"
})
public class PurchasePlanItemResponse {

    private String category;

    private String itemName;

    private Integer quantity;

    private BigDecimal estimatedUnitPrice;

    private BigDecimal totalAmount;

    public static PurchasePlanItemResponse from(PurchasePlanItem item) {
        return PurchasePlanItemResponse.builder()
                .category(resolveCategory(item))
                .itemName(item.getItemName())
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
