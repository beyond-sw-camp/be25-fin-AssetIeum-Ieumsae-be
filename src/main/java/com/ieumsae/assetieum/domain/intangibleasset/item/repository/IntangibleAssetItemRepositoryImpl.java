package com.ieumsae.assetieum.domain.intangibleasset.item.repository;

import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
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

import static com.ieumsae.assetieum.domain.intangibleasset.asset.entity.QIntangibleAsset.intangibleAsset;
import static com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.QIntangibleAssetAssignment.intangibleAssetAssignment;
import static com.ieumsae.assetieum.domain.intangibleasset.category.entity.QIntangibleAssetCategory.intangibleAssetCategory;
import static com.ieumsae.assetieum.domain.intangibleasset.item.entity.QIntangibleAssetItem.intangibleAssetItem;

@Repository
@RequiredArgsConstructor
public class IntangibleAssetItemRepositoryImpl implements IntangibleAssetItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final IntangibleAssetCategoryRepository categoryRepository;

    /**
     * 회사 기준 무형자산 품목 목록을 조회한다.
     * 카테고리, 품목명, 제공사, 라이선스 유형, 표준 여부 조건을 동적으로 적용한다.
     */
    @Override
    public Page<IntangibleAssetItemResponse> search(
            UUID companyId,
            UUID categoryId,
            String keyword,
            Boolean isStandard,
            Pageable pageable
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(intangibleAssetItem.company.id.eq(companyId));
        condition.and(intangibleAssetItem.deletedAt.isNull());

        List<UUID> categoryIds = getCategoryIds(categoryId, companyId);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            condition.and(intangibleAssetCategory.id.in(categoryIds));
        }

        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            condition.and(
                    intangibleAssetItem.productName.containsIgnoreCase(trimmedKeyword)
                            .or(intangibleAssetItem.provider.containsIgnoreCase(trimmedKeyword))
                            .or(intangibleAssetItem.licenseType.stringValue().containsIgnoreCase(trimmedKeyword))
                            .or(intangibleAssetCategory.name.containsIgnoreCase(trimmedKeyword))
            );
        }

        if (isStandard != null) {
            condition.and(intangibleAssetItem.isStandard.eq(isStandard));
        }

        List<IntangibleAssetItem> items = queryFactory
                .selectFrom(intangibleAssetItem)
                .join(intangibleAssetItem.intangibleAssetCategory, intangibleAssetCategory)
                .where(condition)
                .orderBy(intangibleAssetItem.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Map<UUID, BigDecimal> prePurchasePriceByItemId = findPrePurchasePriceByItemId(companyId, items);
        Map<UUID, Integer> availableSeatCountByItemId = findAvailableSeatCountByItemId(companyId, items);

        List<IntangibleAssetItemResponse> content = items.stream()
                .map(item -> IntangibleAssetItemResponse.from(
                        item,
                        prePurchasePriceByItemId.get(item.getId()),
                        availableSeatCountByItemId.getOrDefault(item.getId(), 0)
                ))
                .collect(Collectors.toList());

        Long total = queryFactory
                .select(intangibleAssetItem.count())
                .from(intangibleAssetItem)
                .join(intangibleAssetItem.intangibleAssetCategory, intangibleAssetCategory)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private Map<UUID, Integer> findAvailableSeatCountByItemId(
            UUID companyId,
            List<IntangibleAssetItem> items
    ) {
        if (items.isEmpty()) {
            return Map.of();
        }

        List<UUID> itemIds = items.stream()
                .map(IntangibleAssetItem::getId)
                .collect(Collectors.toList());

        List<Tuple> assets = queryFactory
                .select(
                        intangibleAsset.intangibleAssetItem.id,
                        intangibleAsset.id,
                        intangibleAsset.seatCount
                )
                .from(intangibleAsset)
                .where(
                        intangibleAsset.company.id.eq(companyId),
                        intangibleAsset.intangibleAssetItem.id.in(itemIds),
                        intangibleAsset.intangibleAssetStatus.in(
                                IntangibleAssetStatus.AVAILABLE,
                                IntangibleAssetStatus.IN_USE
                        )
                )
                .fetch();

        Map<UUID, Long> activeAssignmentCountByAssetId = findActiveAssignmentCountByAssetId(
                companyId,
                assets.stream()
                        .map(asset -> asset.get(intangibleAsset.id))
                        .collect(Collectors.toList())
        );

        Map<UUID, Integer> availableSeatCountByItemId = new HashMap<>();
        for (Tuple asset : assets) {
            UUID itemId = asset.get(intangibleAsset.intangibleAssetItem.id);
            UUID assetId = asset.get(intangibleAsset.id);
            Integer seatCount = asset.get(intangibleAsset.seatCount);
            long activeAssignmentCount = activeAssignmentCountByAssetId.getOrDefault(assetId, 0L);
            int availableSeatCount = Math.max((seatCount == null ? 0 : seatCount) - Math.toIntExact(activeAssignmentCount), 0);
            availableSeatCountByItemId.merge(itemId, availableSeatCount, Integer::sum);
        }

        return availableSeatCountByItemId;
    }

    private Map<UUID, Long> findActiveAssignmentCountByAssetId(
            UUID companyId,
            List<UUID> assetIds
    ) {
        if (assetIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> assignmentCounts = queryFactory
                .select(
                        intangibleAssetAssignment.intangibleAsset.id,
                        intangibleAssetAssignment.count()
                )
                .from(intangibleAssetAssignment)
                .where(
                        intangibleAssetAssignment.company.id.eq(companyId),
                        intangibleAssetAssignment.intangibleAsset.id.in(assetIds),
                        intangibleAssetAssignment.assignmentStatus.eq(AssignmentStatus.ACTIVE)
                )
                .groupBy(intangibleAssetAssignment.intangibleAsset.id)
                .fetch();

        Map<UUID, Long> activeAssignmentCountByAssetId = new HashMap<>();
        for (Tuple assignmentCount : assignmentCounts) {
            UUID assetId = assignmentCount.get(intangibleAssetAssignment.intangibleAsset.id);
            Long count = assignmentCount.get(intangibleAssetAssignment.count());
            activeAssignmentCountByAssetId.put(assetId, count == null ? 0 : count);
        }

        return activeAssignmentCountByAssetId;
    }

    private Map<UUID, BigDecimal> findPrePurchasePriceByItemId(
            UUID companyId,
            List<IntangibleAssetItem> items
    ) {
        if (items.isEmpty()) {
            return Map.of();
        }

        List<UUID> itemIds = items.stream()
                .map(IntangibleAssetItem::getId)
                .collect(Collectors.toList());

        List<Tuple> latestAssets = queryFactory
                .select(
                        intangibleAsset.intangibleAssetItem.id,
                        intangibleAsset.purchasePrice
                )
                .from(intangibleAsset)
                .where(
                        intangibleAsset.company.id.eq(companyId),
                        intangibleAsset.intangibleAssetItem.id.in(itemIds),
                        intangibleAsset.purchaseDate.isNotNull()
                )
                .orderBy(
                        intangibleAsset.intangibleAssetItem.id.asc(),
                        intangibleAsset.purchaseDate.desc(),
                        intangibleAsset.createdAt.desc()
                )
                .fetch();

        Map<UUID, BigDecimal> prePurchasePriceByItemId = new HashMap<>();
        for (Tuple latestAsset : latestAssets) {
            UUID itemId = latestAsset.get(intangibleAsset.intangibleAssetItem.id);
            BigDecimal purchasePrice = latestAsset.get(intangibleAsset.purchasePrice);
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
