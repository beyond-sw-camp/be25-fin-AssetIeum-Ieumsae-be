package com.ieumsae.assetieum.domain.tangibleasset.item.repository;

import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface TangibleAssetItemRepository
        extends JpaRepository<TangibleAssetItem, UUID>, TangibleAssetItemRepositoryCustom  {

    boolean existsByCompany_IdAndProductName(UUID companyId, String productName);

    boolean existsByCompany_IdAndModelName(UUID companyId, String modelName);

    boolean existsByCompany_IdAndTangibleAssetCategory_Id(UUID companyId, UUID tangibleAssetCategoryId);

    Optional<TangibleAssetItem> findByIdAndDeletedAtIsNull(UUID itemId);

    Optional<TangibleAssetItem> findByIdAndCompany_IdAndDeletedAtIsNull(UUID itemId, UUID companyId);

    Optional<TangibleAssetItem> findByModelNameAndCompany_Id(String modelName, UUID companyId);
    @Query("""
            select item
            from TangibleAssetItem item
            where item.company.id = :companyId
              and item.deletedAt is null
              and (:categoryId is null or item.tangibleAssetCategory.id = :categoryId)
              and (
                    :keyword is null
                    or lower(item.productName) like lower(concat('%', :keyword, '%'))
                    or lower(item.manufacturer) like lower(concat('%', :keyword, '%'))
                    or lower(item.modelName) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<TangibleAssetItem> searchAssignableItems(UUID companyId, UUID categoryId, String keyword, Pageable pageable);
}
