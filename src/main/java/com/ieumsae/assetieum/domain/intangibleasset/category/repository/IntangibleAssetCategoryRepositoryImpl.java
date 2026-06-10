package com.ieumsae.assetieum.domain.intangibleasset.category.repository;

import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 무형자산 카테고리 커스텀 Repository 구현체
 * 선택한 카테고리의 하위 카테고리 ID 조회를 담당한다.
 */
@Repository
@RequiredArgsConstructor
public class IntangibleAssetCategoryRepositoryImpl implements IntangibleAssetCategoryRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    /**
     * 특정 카테고리의 모든 하위 카테고리 ID를 조회한다.
     * 선택한 카테고리 하위의 전체 트리를 순회하며
     * 자식, 손자 카테고리까지 포함한 카테고리 ID 목록을 반환한다.
     */
    @Override
    public List<UUID> findAllDescendantIds(UUID categoryId) {
        List<IntangibleAssetCategory> categories = em.createQuery(
                "SELECT c FROM IntangibleAssetCategory c", IntangibleAssetCategory.class
        ).getResultList();

        List<UUID> descendantIds = new ArrayList<>();

        collectDescendantIds(
                categoryId,
                categories,
                descendantIds
        );

        return descendantIds;
    }

    /**
     * 부모 카테고리 기준으로 하위 카테고리를 재귀 탐색한다.
     */
    private void collectDescendantIds(
            UUID parentId,
            List<IntangibleAssetCategory> categories,
            List<UUID> descendantIds
    ) {
        for (IntangibleAssetCategory category : categories) {
            if (category.getParent() == null) {
                continue;
            }

            if (!category.getParent().getId().equals(parentId)) {
                continue;
            }

            descendantIds.add(category.getId());

            collectDescendantIds(
                    category.getId(),
                    categories,
                    descendantIds
            );
        }
    }
}
