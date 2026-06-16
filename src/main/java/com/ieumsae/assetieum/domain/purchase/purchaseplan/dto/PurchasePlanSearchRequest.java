package com.ieumsae.assetieum.domain.purchase.purchaseplan.dto;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PurchasePlanSearchRequest extends PaginationRequest {

    private PurchaseRequestStatus status;

    private UUID requesterId;

    private String keyword;

}
