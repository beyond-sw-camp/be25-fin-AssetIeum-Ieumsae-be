package com.ieumsae.assetieum.domain.purchase.purchaseplan.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "totalCount",
        "approvalWaitingCount",
        "orderedCount",
        "completedCount"
})
public class PurchasePlanStatisticResponse {

    private Long totalCount;

    private Long approvalWaitingCount;

    private Long orderedCount;

    private Long completedCount;

}