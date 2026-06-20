package com.ieumsae.assetieum.domain.purchase.purchaseplan.repository;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanSearchResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanStatisticResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PurchasePlanRepositoryCustom {
    Page<PurchasePlanSearchResponse> search(UUID companyId, PurchaseRequestStatus status, UUID requesterId, String keyword, Pageable pageable);

    PurchasePlanStatisticResponse getPurchasePlanStatistics(UUID companyId);
}
