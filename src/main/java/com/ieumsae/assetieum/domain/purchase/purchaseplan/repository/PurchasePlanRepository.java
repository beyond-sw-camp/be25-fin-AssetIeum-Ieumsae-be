package com.ieumsae.assetieum.domain.purchase.purchaseplan.repository;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlan;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchasePlanRepository extends JpaRepository<PurchasePlan, UUID>, PurchasePlanRepositoryCustom{
    Optional<PurchasePlan> findByIdAndDeletedAtIsNullAndCompany_Id(UUID planId, UUID companyId);
}
