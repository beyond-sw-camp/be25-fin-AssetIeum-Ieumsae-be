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
public class PurchasePlanItemDetailResponse {

    private AssetType assetType;

    private PurchasePlanItemStatus status;

    private Boolean isStandard;

    private UUID categoryId;

    private String categoryName;

    private String productName;

    private Integer quantity;

    private BigDecimal estimatedUnitPrice;

    private BigDecimal totalAmount;

    private UUID ticketRequesterId;

    private String ticketRequesterName;

    private UUID ticketDepartmentId;

    private String ticketDepartmentName;

    public static PurchasePlanItemDetailResponse from(PurchasePlanItem item, String categoryName) {
        return PurchasePlanItemDetailResponse.builder()
                .assetType(item.getAssetType())
                .status(item.getPurchasePlanItemStatus())
                .isStandard(item.getIsStandard())
                .categoryId(item.getCategoryId())
                .categoryName(categoryName)
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
}
