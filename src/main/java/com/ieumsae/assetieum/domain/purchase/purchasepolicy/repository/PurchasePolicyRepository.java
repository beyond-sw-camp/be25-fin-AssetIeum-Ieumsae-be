package com.ieumsae.assetieum.domain.purchase.purchasepolicy.repository;

import com.ieumsae.assetieum.domain.purchase.purchasepolicy.entity.PurchasePolicy;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchasePolicyRepository extends JpaRepository<PurchasePolicy, UUID> {

    Optional<PurchasePolicy> findByCompany_Id(UUID companyId);
}
