package com.ieumsae.assetieum.domain.purchase.purchasepolicy.dto;

import com.ieumsae.assetieum.domain.purchase.purchasepolicy.entity.PurchasePolicy;
import com.ieumsae.assetieum.domain.purchase.purchasepolicy.type.PurchaseMethod;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePolicyResponse {

    private UUID policyId;

    private PurchaseMethod purchaseMethod;

    private BigDecimal overPercentageLimit;

    public static PurchasePolicyResponse from(PurchasePolicy purchasePolicy) {
        return PurchasePolicyResponse.builder()
                .policyId(purchasePolicy.getId())
                .purchaseMethod(purchasePolicy.getPurchaseMethod())
                .overPercentageLimit(purchasePolicy.getOverPercentageLimit())
                .build();
    }
}
