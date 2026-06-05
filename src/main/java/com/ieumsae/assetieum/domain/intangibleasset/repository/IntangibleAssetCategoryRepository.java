package com.ieumsae.assetieum.domain.intangibleasset.repository;

import com.ieumsae.assetieum.domain.intangibleasset.entity.IntangibleAssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IntangibleAssetCategoryRepository extends JpaRepository<IntangibleAssetCategory, UUID> {

    boolean existsByCompany_IdAndName(
            UUID companyId,
            String name);
}
