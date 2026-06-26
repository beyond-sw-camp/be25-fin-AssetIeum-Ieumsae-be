package com.ieumsae.assetieum.domain.purchase.purchaseplan.dto;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchasePlanUpdateStatusRequest {

    @NotNull
    private PurchaseRequestStatus status;

}
