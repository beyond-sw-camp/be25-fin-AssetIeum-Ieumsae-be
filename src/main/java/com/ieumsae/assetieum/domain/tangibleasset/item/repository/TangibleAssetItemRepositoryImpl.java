package com.ieumsae.assetieum.domain.tangibleasset.item.repository;

import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ieumsae.assetieum.domain.tangibleasset.item.entity.QTangibleAssetItem.tangibleAssetItem;

/**
 * 유형자산 품목 QueryDSL 커스텀 Repository 구현체.
 * 품목 목록 조회 시 동적 검색 조건과 페이징 처리를 담당한다.
 */
@RequiredArgsConstructor
public class TangibleAssetItemRepositoryImpl implements TangibleAssetItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final TangibleAssetCategoryRepository categoryRepository;

    /**
     * 회사 기준 유형자산 품목 목록을 조회한다.
     * 카테고리, 품목명, 제조사, 모델명, 표준 여부 조건을 동적으로 적용한다.
     *
     * categoryId가 전달된 경우 선택한 카테고리와 하위 카테고리에 속한 품목까지 함께 조회한다.
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
        List<UUID> categoryIds = getCategoryIds(categoryId);

        List<TangibleAssetItem> content = queryFactory
                .selectFrom(tangibleAssetItem)
                .where(
                        companyIdEq(companyId),
                        categoryIn(categoryIds),
                        productNameContains(productName),
                        manufacturerContains(manufacturer),
                        modelNameContains(modelName),
                        isStandardEq(isStandard),
                        notDeleted()
                )
                .orderBy(tangibleAssetItem.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(tangibleAssetItem.count())
                .from(tangibleAssetItem)
                .where(
                        companyIdEq(companyId),
                        categoryIn(categoryIds),
                        productNameContains(productName),
                        manufacturerContains(manufacturer),
                        modelNameContains(modelName),
                        isStandardEq(isStandard),
                        notDeleted()
                )
                .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total == null ? 0 : total
        );
    }

    private BooleanExpression companyIdEq(UUID companyId) {
        return tangibleAssetItem.company.id.eq(companyId);
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

    private BooleanExpression categoryIn(List<UUID> categoryIds) {
        return categoryIds == null
                ? null
                : tangibleAssetItem.tangibleAssetCategory.id.in(categoryIds);
    }

    private BooleanExpression productNameContains(String productName) {
        return isBlank(productName)
                ? null
                : tangibleAssetItem.productName.containsIgnoreCase(productName);
    }

    private BooleanExpression manufacturerContains(String manufacturer) {
        return isBlank(manufacturer)
                ? null
                : tangibleAssetItem.manufacturer.containsIgnoreCase(manufacturer);
    }

    private BooleanExpression modelNameContains(String modelName) {
        return isBlank(modelName)
                ? null
                : tangibleAssetItem.modelName.containsIgnoreCase(modelName);
    }

    private BooleanExpression isStandardEq(Boolean isStandard) {
        return isStandard == null
                ? null
                : tangibleAssetItem.isStandard.eq(isStandard);
    }

    private BooleanExpression notDeleted() {
        return tangibleAssetItem.deletedAt.isNull();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}