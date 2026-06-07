package com.ieumsae.assetieum.domain.tangibleasset.item.repository;

import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 회사 기준 유형자산 품목 목록을 조회한다.
 *
 * 카테고리, 품목명, 제조사, 모델명,
 * 표준 품목 여부 조건으로 필터링하여,
 * 페이징 처리된 결과를 반환한다.
 *
 * 카테고리 검색 시 상위 카테고리를 선택하면
 * 하위 카테고리 품목까지 함께 조회한다.
 */
public interface TangibleAssetItemRepositoryCustom {
    Page<TangibleAssetItem> search(
            UUID companyId,
            UUID categoryId,
            String productName,
            String manufacturer,
            String modelName,
            Boolean isStandard,
            Pageable pageable
    );
}
