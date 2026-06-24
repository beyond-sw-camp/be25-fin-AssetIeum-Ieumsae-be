package com.ieumsae.assetieum.domain.purchase.purchaseplan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PurchasePlanPurchaseResultRequest {

    @NotNull(message = "실제 결제 금액은 필수입니다.")
    @DecimalMin(value = "0.00", message = "실제 결제 금액은 0 이상이어야 합니다.")
    private BigDecimal actualAmount;
}
