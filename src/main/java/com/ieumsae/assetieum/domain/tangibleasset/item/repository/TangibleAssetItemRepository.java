package com.ieumsae.assetieum.domain.tangibleasset.item.repository;

import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TangibleAssetItemRepository
        extends JpaRepository<TangibleAssetItem, UUID>, TangibleAssetItemRepositoryCustom  {

    boolean existsByCompany_IdAndProductName(UUID companyId, String productName);

    boolean existsByCompany_IdAndModelName(UUID companyId, String modelName);

    boolean existsByCompany_IdAndTangibleAssetCategory_Id(UUID companyId, UUID tangibleAssetCategoryId);

    Optional<TangibleAssetItem> findByIdAndDeletedAtIsNull(UUID itemId);

    Optional<TangibleAssetItem> findByIdAndCompany_IdAndDeletedAtIsNull(UUID itemId, UUID companyId);
}
