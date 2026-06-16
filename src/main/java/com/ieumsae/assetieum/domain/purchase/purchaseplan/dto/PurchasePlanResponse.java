package com.ieumsae.assetieum.domain.purchase.purchaseplan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlan;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "planId",
    "planNo",
    "estimatedAmount",
    "itemCount",
    "createdAt",
    "updatedAt"
})
public class PurchasePlanResponse {

    private UUID planId;

    private String planNo;

    private BigDecimal estimatedAmount;

    private int itemCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static PurchasePlanResponse from(PurchasePlan purchasePlan, int itemCount) {
        return PurchasePlanResponse.builder()
                .planId(purchasePlan.getId())
                .planNo(purchasePlan.getPlanNo())
                .estimatedAmount(purchasePlan.getEstimatedAmount())
                .itemCount(itemCount)
                .createdAt(purchasePlan.getCreatedAt())
                .updatedAt(purchasePlan.getUpdatedAt())
                .build();
    }
}
