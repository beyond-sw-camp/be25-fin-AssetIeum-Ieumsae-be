package com.ieumsae.assetieum.domain.purchase.purchaseplanitem.repository;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchasePlanItemRepository extends JpaRepository<PurchasePlanItem, Long> {

    Optional<List<PurchasePlanItem>> findAllByPurchasePlan_IdAndCompany_Id(UUID planId, UUID companyId);

    Optional<PurchasePlanItem> findByIdAndCompany_Id(Long id, UUID company_id);

    Optional<PurchasePlanItem> findByIdAndPurchasePlan_IdAndCompany_Id(Long id, UUID planId, UUID companyId);
}
