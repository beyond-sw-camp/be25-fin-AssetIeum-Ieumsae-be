package com.ieumsae.assetieum.domain.intangibleasset.item.repository;

import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
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

@Repository
@RequiredArgsConstructor
public class IntangibleAssetItemRepositoryImpl implements IntangibleAssetItemRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;
    private final IntangibleAssetCategoryRepository categoryRepository;

    /**
     * 회사 기준 무형자산 품목 목록을 조회한다.
     * 카테고리, 품목명, 제공사, 라이선스 유형, 표준 여부 조건을 동적으로 적용한다.
     */
    @Override
    public Page<IntangibleAssetItem> search(
            UUID companyId,
            UUID categoryId,
            String keyword,
            Boolean isStandard,
            Pageable pageable
    ) {
        StringBuilder jpql = new StringBuilder("SELECT t FROM IntangibleAssetItem t WHERE t.company.id = :companyId AND t.deletedAt IS NULL");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(t) FROM IntangibleAssetItem t WHERE t.company.id = :companyId AND t.deletedAt IS NULL");

        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);

        List<UUID> categoryIds = getCategoryIds(categoryId);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            jpql.append(" AND t.intangibleAssetCategory.id IN :categoryIds");
            countJpql.append(" AND t.intangibleAssetCategory.id IN :categoryIds");
            params.put("categoryIds", categoryIds);
        }

        if (keyword != null && !keyword.isBlank()) {
            String keywordCondition = """
                     AND (
                        LOWER(t.productName) LIKE LOWER(:keyword)
                        OR LOWER(t.provider) LIKE LOWER(:keyword)
                        OR LOWER(CAST(t.licenseType AS string)) LIKE LOWER(:keyword)
                        OR LOWER(t.intangibleAssetCategory.name) LIKE LOWER(:keyword)
                    )
                    """;
            jpql.append(keywordCondition);
            countJpql.append(keywordCondition);
            params.put("keyword", "%" + keyword + "%");
        }

        if (isStandard != null) {
            jpql.append(" AND t.isStandard = :isStandard");
            countJpql.append(" AND t.isStandard = :isStandard");
            params.put("isStandard", isStandard);
        }

        jpql.append(" ORDER BY t.createdAt DESC");

        TypedQuery<IntangibleAssetItem> query = em.createQuery(jpql.toString(), IntangibleAssetItem.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<IntangibleAssetItem> content = query.getResultList();
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
