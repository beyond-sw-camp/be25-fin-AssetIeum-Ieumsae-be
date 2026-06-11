package com.ieumsae.assetieum.domain.tangibleasset.category.repository;

import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TangibleAssetCategoryRepository
        extends JpaRepository<TangibleAssetCategory, UUID>, TangibleAssetCategoryRepositoryCustom {
    boolean existsByCompany_IdAndName(
            UUID companyId,
            String name
    );

    List<TangibleAssetCategory> findAllByCompany_IdOrderByCreatedAtAsc(UUID companyId);

    Optional<TangibleAssetCategory> findByIdAndCompany_Id(UUID categoryId, UUID companyId);

    boolean existsByParent_IdAndCompany_Id(UUID categoryId, UUID companyId);
}