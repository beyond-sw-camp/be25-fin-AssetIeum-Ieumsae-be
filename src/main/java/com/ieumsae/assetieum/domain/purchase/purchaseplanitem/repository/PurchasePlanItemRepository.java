package com.ieumsae.assetieum.domain.purchase.purchaseplanitem.repository;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchasePlanItemRepository extends JpaRepository<PurchasePlanItem, Long> {

    List<PurchasePlanItem> findAllByPurchasePlan_IdAndCompany_Id(UUID planId, UUID companyId);
}
