package com.ieumsae.assetieum.domain.tangibleasset.repository;

import com.ieumsae.assetieum.domain.tangibleasset.entity.TangibleAssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TangibleAssetCategoryRepository extends JpaRepository<TangibleAssetCategory, UUID> {
    boolean existsByCompany_IdAndName(
            UUID companyId,
            String name
    );

    @Query("""
        SELECT c
        FROM TangibleAssetCategory c
        WHERE c.company.id = :companyId
          AND (:parentId IS NULL OR c.parent.id = :parentId)
        ORDER BY c.createdAt DESC
        """)
    List<TangibleAssetCategory> search(
            @Param("companyId") UUID companyId,
            @Param("parentId") UUID parentId);

    List<TangibleAssetCategory> findAllByCompany_IdOrderByCreatedAtAsc(UUID companyId);
}