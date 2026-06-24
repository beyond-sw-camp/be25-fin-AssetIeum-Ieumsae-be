package com.ieumsae.assetieum.domain.purchase.purchaseplanitem.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchasePlanItemStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "assetType",
    "status",
    "isStandard",
    "categoryId",
    "categoryName",
    "productName",
    "quantity",
    "estimatedUnitPrice",
    "totalAmount"
})
public class PurchasePlanItemResponse {

    private AssetType assetType;

    private PurchasePlanItemStatus status;

    private Boolean isStandard;

    private UUID categoryId;

    private String categoryName;

    private String productName;

    private Integer quantity;

    private BigDecimal estimatedUnitPrice;

    private BigDecimal totalAmount;

    public static PurchasePlanItemResponse from(PurchasePlanItem item) {
        return PurchasePlanItemResponse.builder()
                .assetType(item.getAssetType())
                .status(item.getPurchasePlanItemStatus())
                .isStandard(item.getIsStandard())
                .categoryId(item.getCategoryId())
                .categoryName(resolveCategoryName(item))
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .estimatedUnitPrice(item.getEstimatedUnitPrice())
                .totalAmount(item.getEstimatedUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    private static String resolveCategoryName(PurchasePlanItem item) {
        if (item.getTangibleAssetItem() != null) {
            return item.getTangibleAssetItem().getTangibleAssetCategory().getName();
        }

        if (item.getIntangibleAssetItem() != null) {
            return item.getIntangibleAssetItem().getIntangibleAssetCategory().getName();
        }

        return null;
    }
}
