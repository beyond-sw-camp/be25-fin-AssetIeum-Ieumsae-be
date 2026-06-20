package com.ieumsae.assetieum.domain.purchase.purchaseplan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlan;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "planId",
        "planNo",
        "purchaseRequestStatus",
        "estimatedAmount",
        "itemCount",
        "itemName",
        "requesterName",
        "createdAt",
        "updatedAt",
        "deletedAt"
})
public class PurchasePlanSearchResponse {

    private UUID planId;

    private String planNo;

    private PurchaseRequestStatus purchaseRequestStatus;

    private BigDecimal estimatedAmount;

    private Integer itemCount;

    private String itemName;

    private String requesterName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;

    public static PurchasePlanSearchResponse from(PurchasePlan purchasePlan, String itemName, String requesterName) {
        return PurchasePlanSearchResponse.builder()
                .planId(purchasePlan.getId())
                .planNo(purchasePlan.getPlanNo())
                .purchaseRequestStatus(purchasePlan.getPurchaseRequestStatus())
                .estimatedAmount(purchasePlan.getEstimatedAmount())
                .itemCount(purchasePlan.getItemCount())
                .itemName(itemName)
                .requesterName(requesterName)
                .createdAt(purchasePlan.getCreatedAt())
                .updatedAt(purchasePlan.getUpdatedAt())
                .deletedAt(purchasePlan.getDeletedAt())
                .build();
    }
}
