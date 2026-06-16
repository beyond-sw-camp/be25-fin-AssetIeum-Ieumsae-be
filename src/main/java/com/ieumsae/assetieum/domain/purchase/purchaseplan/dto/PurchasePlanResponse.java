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
    "createdAt",
    "updatedAt",
    "deletedAt"
})
public class PurchasePlanResponse {

    private UUID planId;

    private String planNo;

    private PurchaseRequestStatus purchaseRequestStatus;

    private BigDecimal estimatedAmount;

    private Integer itemCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;

    public static PurchasePlanResponse from(PurchasePlan purchasePlan) {
        return PurchasePlanResponse.builder()
                .planId(purchasePlan.getId())
                .planNo(purchasePlan.getPlanNo())
                .purchaseRequestStatus(purchasePlan.getPurchaseRequestStatus())
                .estimatedAmount(purchasePlan.getEstimatedAmount())
                .itemCount(purchasePlan.getItemCount())
                .createdAt(purchasePlan.getCreatedAt())
                .updatedAt(purchasePlan.getUpdatedAt())
                .deletedAt(purchasePlan.getDeletedAt())
                .build();
    }
}
