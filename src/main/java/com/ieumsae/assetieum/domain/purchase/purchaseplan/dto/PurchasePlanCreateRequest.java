package com.ieumsae.assetieum.domain.purchase.purchaseplan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.dto.PurchasePlanItemCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchasePlanCreateRequest {

    @Valid
    @NotEmpty(message = "구매 계획 품목은 1개 이상이어야 합니다.")
    private List<PurchasePlanItemCreateRequest> items;

}
