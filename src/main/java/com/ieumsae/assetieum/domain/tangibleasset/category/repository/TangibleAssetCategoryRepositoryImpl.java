package com.ieumsae.assetieum.domain.tangibleasset.category.repository;

import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ieumsae.assetieum.domain.tangibleasset.category.entity.QTangibleAssetCategory.tangibleAssetCategory;

/**
 * 유형자산 카테고리 QueryDSL 커스텀 Repository 구현체.
 * 카테고리 ID 조회 시 동적 검색 조건 처리를 담당한다.
 */
@RequiredArgsConstructor
public class TangibleAssetCategoryRepositoryImpl implements TangibleAssetCategoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 카테고리의 모든 하위 카테고리 ID를 조회한다.
     * 선택한 카테고리 하위의 전체 트리를 순회하며
     * 자식, 손자 카테고리까지 포함한 카테고리 ID 목록을 반환한다.
     */
    @Override
    public List<UUID> findAllDescendantIds(UUID categoryId) {

        // 전체 카테고리 조회
        List<TangibleAssetCategory> categories =
                queryFactory
                        .selectFrom(tangibleAssetCategory)
                        .fetch();

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
            List<TangibleAssetCategory> categories,
            List<UUID> descendantIds
    ) {
        for(TangibleAssetCategory category : categories) {
            if(category.getParent() == null) {
                continue;
            }

            if(!category.getParent().getId().equals(parentId)){
                continue;
            }

            descendantIds.add(category.getId());

            // 재귀 탐색
            collectDescendantIds(
                    category.getId(),
                    categories,
                    descendantIds
            );
        }
    }
}
