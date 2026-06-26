package com.ieumsae.assetieum.domain.purchase.purchaseplan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlan;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.dto.PurchasePlanItemDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "planId",
    "planNo",
    "requesterId",
    "requesterName",
    "purchaseRequestStatus",
    "createdAt",
    "updatedAt",
    "estimatedAmount",
    "actualAmount",
    "items"
})
public class PurchasePlanDetailResponse {

    private UUID planId;

    private String planNo;

    private UUID requesterId;

    private String requesterName;

    private PurchaseRequestStatus purchaseRequestStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private BigDecimal estimatedAmount;

    private BigDecimal actualAmount;

    private List<PurchasePlanItemDetailResponse> items;

    public static PurchasePlanDetailResponse from(
            PurchasePlan purchasePlan,
            List<PurchasePlanItemDetailResponse> items
    ) {
        return PurchasePlanDetailResponse.builder()
                .planId(purchasePlan.getId())
                .planNo(purchasePlan.getPlanNo())
                .requesterId(purchasePlan.getRequester().getId())
                .requesterName(purchasePlan.getRequester().getName())
                .purchaseRequestStatus(purchasePlan.getPurchaseRequestStatus())
                .createdAt(purchasePlan.getCreatedAt())
                .updatedAt(purchasePlan.getUpdatedAt())
                .estimatedAmount(purchasePlan.getEstimatedAmount())
                .actualAmount(purchasePlan.getActualAmount())
                .items(items)
                .build();
    }
}
