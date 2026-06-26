package com.ieumsae.assetieum.domain.purchase.purchasepolicy.dto;

import com.ieumsae.assetieum.domain.purchase.purchasepolicy.type.PurchaseMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PurchasePolicyRequest {
    private PurchaseMethod purchaseMethod;

    private BigDecimal overPercentageLimit;
}
