package com.ieumsae.assetieum.domain.tangibleasset.item.repository;

import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ieumsae.assetieum.domain.tangibleasset.asset.entity.QTangibleAsset.tangibleAsset;
import static com.ieumsae.assetieum.domain.tangibleasset.category.entity.QTangibleAssetCategory.tangibleAssetCategory;
import static com.ieumsae.assetieum.domain.tangibleasset.item.entity.QTangibleAssetItem.tangibleAssetItem;

/**
 * 유형자산 품목 Repository 구현체.
 * QueryDSL 사용하여 동적 쿼리를 처리한다.
 */
@Repository
@RequiredArgsConstructor
public class TangibleAssetItemRepositoryImpl implements TangibleAssetItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final TangibleAssetCategoryRepository categoryRepository;

    /**
     * 회사 기준 유형자산 품목 목록을 조회한다.
     * 카테고리, 품목명, 제조사, 모델명, 표준 여부 조건을 동적으로 적용한다.
     */
    @Override
    public Page<TangibleAssetItemResponse> search(
            UUID companyId,
            UUID categoryId,
            String keyword,
            Boolean isStandard,
            Pageable pageable
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(tangibleAssetItem.company.id.eq(companyId));
        condition.and(tangibleAssetItem.deletedAt.isNull());

        List<UUID> categoryIds = getCategoryIds(categoryId, companyId);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            condition.and(tangibleAssetCategory.id.in(categoryIds));
        }

        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            condition.and(
                    tangibleAssetItem.productName.containsIgnoreCase(trimmedKeyword)
                            .or(tangibleAssetItem.manufacturer.containsIgnoreCase(trimmedKeyword))
                            .or(tangibleAssetItem.modelName.containsIgnoreCase(trimmedKeyword))
                            .or(tangibleAssetCategory.name.containsIgnoreCase(trimmedKeyword))
            );
        }

        if (isStandard != null) {
            condition.and(tangibleAssetItem.isStandard.eq(isStandard));
        }

        List<TangibleAssetItem> items = queryFactory
                .selectFrom(tangibleAssetItem)
                .join(tangibleAssetItem.tangibleAssetCategory, tangibleAssetCategory)
                .where(condition)
                .orderBy(tangibleAssetItem.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Map<UUID, BigDecimal> prePurchasePriceByItemId = findPrePurchasePriceByItemId(companyId, items);
        Map<UUID, Integer> availableAssetCountByItemId = findAvailableAssetCountByItemId(companyId, items);

        List<TangibleAssetItemResponse> content = items.stream()
                .map(item -> TangibleAssetItemResponse.from(
                        item,
                        prePurchasePriceByItemId.get(item.getId()),
                        availableAssetCountByItemId.getOrDefault(item.getId(), 0)
                ))
                .collect(Collectors.toList());

        Long total = queryFactory
                .select(tangibleAssetItem.count())
                .from(tangibleAssetItem)
                .join(tangibleAssetItem.tangibleAssetCategory, tangibleAssetCategory)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private Map<UUID, Integer> findAvailableAssetCountByItemId(
            UUID companyId,
            List<TangibleAssetItem> items
    ) {
        if (items.isEmpty()) {
            return Map.of();
        }

        List<UUID> itemIds = items.stream()
                .map(TangibleAssetItem::getId)
                .collect(Collectors.toList());

        List<Tuple> availableAssetCounts = queryFactory
                .select(
                        tangibleAsset.tangibleAssetItem.id,
                        tangibleAsset.count()
                )
                .from(tangibleAsset)
                .where(
                        tangibleAsset.company.id.eq(companyId),
                        tangibleAsset.tangibleAssetItem.id.in(itemIds),
                        tangibleAsset.tangibleAssetStatus.eq(TangibleAssetStatus.AVAILABLE)
                )
                .groupBy(tangibleAsset.tangibleAssetItem.id)
                .fetch();

        Map<UUID, Integer> availableAssetCountByItemId = new HashMap<>();
        for (Tuple availableAssetCount : availableAssetCounts) {
            UUID itemId = availableAssetCount.get(tangibleAsset.tangibleAssetItem.id);
            Long count = availableAssetCount.get(tangibleAsset.count());
            availableAssetCountByItemId.put(itemId, count == null ? 0 : Math.toIntExact(count));
        }

        return availableAssetCountByItemId;
    }

    private Map<UUID, BigDecimal> findPrePurchasePriceByItemId(
            UUID companyId,
            List<TangibleAssetItem> items
    ) {
        if (items.isEmpty()) {
            return Map.of();
        }

        List<UUID> itemIds = items.stream()
                .map(TangibleAssetItem::getId)
                .collect(Collectors.toList());

        List<Tuple> latestAssets = queryFactory
                .select(
                        tangibleAsset.tangibleAssetItem.id,
                        tangibleAsset.purchasePrice
                )
                .from(tangibleAsset)
                .where(
                        tangibleAsset.company.id.eq(companyId),
                        tangibleAsset.tangibleAssetItem.id.in(itemIds),
                        tangibleAsset.purchaseDate.isNotNull()
                )
                .orderBy(
                        tangibleAsset.tangibleAssetItem.id.asc(),
                        tangibleAsset.purchaseDate.desc(),
                        tangibleAsset.createdAt.desc()
                )
                .fetch();

        Map<UUID, BigDecimal> prePurchasePriceByItemId = new HashMap<>();
        for (Tuple latestAsset : latestAssets) {
            UUID itemId = latestAsset.get(tangibleAsset.tangibleAssetItem.id);
            BigDecimal purchasePrice = latestAsset.get(tangibleAsset.purchasePrice);
            prePurchasePriceByItemId.putIfAbsent(itemId, purchasePrice);
        }

        return prePurchasePriceByItemId;
    }

    private List<UUID> getCategoryIds(UUID categoryId, UUID companyId) {
        if (categoryId == null) {
            return null;
        }

        List<UUID> categoryIds = new ArrayList<>(
                categoryRepository.findAllDescendantIds(categoryId, companyId)
        );

        categoryIds.add(categoryId);

        return categoryIds;
    }
}
