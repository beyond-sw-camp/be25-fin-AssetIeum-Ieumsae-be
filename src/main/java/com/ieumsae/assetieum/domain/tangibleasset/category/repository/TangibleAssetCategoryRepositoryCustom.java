package com.ieumsae.assetieum.domain.tangibleasset.category.repository;

import java.util.List;
import java.util.UUID;

public interface TangibleAssetCategoryRepositoryCustom {

    /**
     * 특정 카테고리의 모든 하위 카테고리 ID를 조회한다.
     * 선택한 카테고리 하위의 전체 트리를 순회하며
     * 자식, 손자 카테고리까지 포함한 카테고리 ID 목록을 반환한다.
     */
    List<UUID> findAllDescendantIds(UUID categoryId, UUID companyId);

}
