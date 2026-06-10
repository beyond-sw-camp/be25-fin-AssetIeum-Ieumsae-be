package com.ieumsae.assetieum.domain.intangibleasset.category.repository;

import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IntangibleAssetCategoryRepository extends JpaRepository<IntangibleAssetCategory, UUID>, IntangibleAssetCategoryRepositoryCustom{

    boolean existsByCompany_IdAndName(
            UUID companyId,
            String name);

    List<IntangibleAssetCategory> findAllByCompany_IdOrderByCreatedAtAsc(UUID companyId);

    boolean existsByParent_Id(UUID categoryId);
}
