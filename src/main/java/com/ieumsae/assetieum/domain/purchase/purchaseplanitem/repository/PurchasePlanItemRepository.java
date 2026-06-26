package com.ieumsae.assetieum.domain.purchase.purchaseplanitem.repository;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchasePlanItemRepository extends JpaRepository<PurchasePlanItem, Long> {

    Optional<List<PurchasePlanItem>> findAllByPurchasePlan_IdAndCompany_Id(UUID planId, UUID companyId);

    Optional<PurchasePlanItem> findByIdAndCompany_Id(Long id, UUID company_id);

    Optional<PurchasePlanItem> findByIdAndPurchasePlan_IdAndCompany_Id(Long id, UUID planId, UUID companyId);

    Optional<PurchasePlanItem> findFirstByTicket_IdAndCompany_IdAndPurchasePlan_PurchaseRequestStatusNotInOrderByIdDesc(
            UUID ticketId,
            UUID companyId,
            Collection<PurchaseRequestStatus> excludedStatuses
    );

    List<PurchasePlanItem> findAllByTicket_IdAndCompany_Id(UUID ticketId, UUID companyId);
}
