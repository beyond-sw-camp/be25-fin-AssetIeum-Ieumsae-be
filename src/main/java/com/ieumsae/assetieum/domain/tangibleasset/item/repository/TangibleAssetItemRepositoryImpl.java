package com.ieumsae.assetieum.domain.tangibleasset.item.repository;

import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 유형자산 품목 Repository 구현체.
 * JPA EntityManager를 사용하여 동적 쿼리를 처리한다.
 */
@Repository
@RequiredArgsConstructor
public class TangibleAssetItemRepositoryImpl implements TangibleAssetItemRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;
    private final TangibleAssetCategoryRepository categoryRepository;

    /**
     * 회사 기준 유형자산 품목 목록을 조회한다.
     * 카테고리, 품목명, 제조사, 모델명, 표준 여부 조건을 동적으로 적용한다.
     */
    @Override
    public Page<TangibleAssetItem> search(
            UUID companyId,
            UUID categoryId,
            String productName,
            String manufacturer,
            String modelName,
            Boolean isStandard,
            Pageable pageable
    ) {
        StringBuilder jpql = new StringBuilder("SELECT t FROM TangibleAssetItem t WHERE t.company.id = :companyId AND t.deletedAt IS NULL");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(t) FROM TangibleAssetItem t WHERE t.company.id = :companyId AND t.deletedAt IS NULL");
        
        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);

        List<UUID> categoryIds = getCategoryIds(categoryId);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            jpql.append(" AND t.tangibleAssetCategory.id IN :categoryIds");
            countJpql.append(" AND t.tangibleAssetCategory.id IN :categoryIds");
            params.put("categoryIds", categoryIds);
        }

        if (productName != null && !productName.isBlank()) {
            jpql.append(" AND LOWER(t.productName) LIKE LOWER(:productName)");
            countJpql.append(" AND LOWER(t.productName) LIKE LOWER(:productName)");
            params.put("productName", "%" + productName + "%");
        }

        if (manufacturer != null && !manufacturer.isBlank()) {
            jpql.append(" AND LOWER(t.manufacturer) LIKE LOWER(:manufacturer)");
            countJpql.append(" AND LOWER(t.manufacturer) LIKE LOWER(:manufacturer)");
            params.put("manufacturer", "%" + manufacturer + "%");
        }

        if (modelName != null && !modelName.isBlank()) {
            jpql.append(" AND LOWER(t.modelName) LIKE LOWER(:modelName)");
            countJpql.append(" AND LOWER(t.modelName) LIKE LOWER(:modelName)");
            params.put("modelName", "%" + modelName + "%");
        }

        if (isStandard != null) {
            jpql.append(" AND t.isStandard = :isStandard");
            countJpql.append(" AND t.isStandard = :isStandard");
            params.put("isStandard", isStandard);
        }

        jpql.append(" ORDER BY t.createdAt DESC");

        TypedQuery<TangibleAssetItem> query = em.createQuery(jpql.toString(), TangibleAssetItem.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<TangibleAssetItem> content = query.getResultList();
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private List<UUID> getCategoryIds(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }

        List<UUID> categoryIds = new ArrayList<>(
                categoryRepository.findAllDescendantIds(categoryId)
        );

        categoryIds.add(categoryId);

        return categoryIds;
    }
}
