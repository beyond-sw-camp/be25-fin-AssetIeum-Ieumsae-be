package com.ieumsae.assetieum.domain.intangibleasset.item.repository;

import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface IntangibleAssetItemRepository extends JpaRepository<IntangibleAssetItem, UUID>, IntangibleAssetItemRepositoryCustom{

    Optional<IntangibleAssetItem> findByIdAndDeletedAtIsNull(UUID itemId);

    Optional<IntangibleAssetItem> findByIdAndCompany_IdAndDeletedAtIsNull(UUID itemId, UUID companyId);

    boolean existsByCompany_IdAndProductName(UUID id, String productName);

    boolean existsByCompany_IdAndIntangibleAssetCategory_Id(UUID companyId, UUID categoryId);

    Optional<IntangibleAssetItem> findByProductNameAndCompany_Id(String trim, UUID companyId);
    @Query("""
            select item
            from IntangibleAssetItem item
            where item.company.id = :companyId
              and item.deletedAt is null
              and (:categoryId is null or item.intangibleAssetCategory.id = :categoryId)
              and (
                    :keyword is null
                    or lower(item.productName) like lower(concat('%', :keyword, '%'))
                    or lower(item.provider) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<IntangibleAssetItem> searchAssignableItems(UUID companyId, UUID categoryId, String keyword, Pageable pageable);
}
