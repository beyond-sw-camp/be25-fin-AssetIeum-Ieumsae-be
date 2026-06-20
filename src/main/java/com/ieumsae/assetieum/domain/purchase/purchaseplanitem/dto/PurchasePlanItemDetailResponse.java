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
        "category",
        "productName",
        "quantity",
        "estimatedUnitPrice",
        "totalAmount"
})
public class PurchasePlanItemDetailResponse {

    private AssetType assetType;

    private PurchasePlanItemStatus status;

    private String category;

    private String productName;

    private Integer quantity;

    private BigDecimal estimatedUnitPrice;

    private BigDecimal totalAmount;

    private UUID ticketRequesterId;

    private String ticketRequesterName;

    private UUID ticketDepartmentId;

    private String ticketDepartmentName;

    public static PurchasePlanItemDetailResponse from(PurchasePlanItem item) {
        return PurchasePlanItemDetailResponse.builder()
                .assetType(item.getAssetType())
                .status(item.getPurchasePlanItemStatus())
                .category(resolveCategory(item))
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .estimatedUnitPrice(item.getEstimatedUnitPrice())
                .totalAmount(item.getEstimatedUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .ticketRequesterId(item.getTicket() != null ? item.getTicket().getRequester().getId() : null)
                .ticketRequesterName(item.getTicket() != null ? item.getTicket().getRequester().getName() : null)
                .ticketDepartmentId(item.getTicket() != null ? item.getTicket().getDepartment().getId() : null)
                .ticketDepartmentName(item.getTicket() != null ? item.getTicket().getDepartment().getName() : null)
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
