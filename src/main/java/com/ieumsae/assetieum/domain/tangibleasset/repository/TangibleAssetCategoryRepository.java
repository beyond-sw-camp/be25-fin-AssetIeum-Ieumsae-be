package com.ieumsae.assetieum.domain.tangibleasset.repository;

import com.ieumsae.assetieum.domain.tangibleasset.entity.TangibleAssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TangibleAssetCategoryRepository extends JpaRepository<TangibleAssetCategory, UUID> {
    boolean existsByCompany_IdAndName(
            UUID companyId,
            String name
    );
}
