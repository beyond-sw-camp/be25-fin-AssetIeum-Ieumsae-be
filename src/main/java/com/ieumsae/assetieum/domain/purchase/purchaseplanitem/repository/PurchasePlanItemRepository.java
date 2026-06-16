package com.ieumsae.assetieum.domain.purchase.purchaseplanitem.repository;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchasePlanItemRepository extends JpaRepository<PurchasePlanItem, Long> {
}
