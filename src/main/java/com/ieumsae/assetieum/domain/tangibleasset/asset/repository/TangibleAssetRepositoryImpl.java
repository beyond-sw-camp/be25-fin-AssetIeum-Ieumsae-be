package com.ieumsae.assetieum.domain.tangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ieumsae.assetieum.domain.department.entity.QDepartment.department;
import static com.ieumsae.assetieum.domain.member.entity.QMember.member;
import static com.ieumsae.assetieum.domain.tangibleasset.asset.entity.QTangibleAsset.tangibleAsset;
import static com.ieumsae.assetieum.domain.tangibleasset.category.entity.QTangibleAssetCategory.tangibleAssetCategory;
import static com.ieumsae.assetieum.domain.tangibleasset.item.entity.QTangibleAssetItem.tangibleAssetItem;

/**
 * 유형자산 Repository 구현체
 * JPA EntityManager를 사용하여 동적 쿼리를 처리한다.
 */
@Repository
@RequiredArgsConstructor
public class TangibleAssetRepositoryImpl implements TangibleAssetRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final TangibleAssetCategoryRepository categoryRepository;

    /**
     * 회사 기준 유형자산 목록을 조회한다.
     * 카테고리, 품목 ID, 상태, 키워드, 현재 사용자, 부서 조건을 동적으로 적용한다.
     */
    @Override
    public Page<TangibleAssetSearchResponse> search(
            UUID companyId,
            UUID categoryId,
            UUID tangibleItemId,
            TangibleAssetStatus status,
            String keyword,
            UUID currentUserId,
            UUID departmentId,
            Pageable pageable
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(tangibleAsset.company.id.eq(companyId));

        List<UUID> categoryIds = getCategoryIds(categoryId);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            condition.and(tangibleAssetCategory.id.in(categoryIds));
        }

        if (tangibleItemId != null) {
            condition.and(tangibleAssetItem.id.eq(tangibleItemId));
        }

        if (status != null) {
            condition.and(tangibleAsset.tangibleAssetStatus.eq(status));
        }

        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            condition.and(
                    tangibleAsset.serialNumber.containsIgnoreCase(trimmedKeyword)
                            .or(tangibleAsset.purchaseVendor.containsIgnoreCase(trimmedKeyword))
                            .or(tangibleAssetItem.productName.containsIgnoreCase(trimmedKeyword))
                            .or(tangibleAssetItem.manufacturer.containsIgnoreCase(trimmedKeyword))
                            .or(tangibleAssetItem.modelName.containsIgnoreCase(trimmedKeyword))
            );
        }

        if (currentUserId != null) {
            condition.and(member.id.eq(currentUserId));
        }

        if (departmentId != null) {
            condition.and(department.id.eq(departmentId));
        }

        List<TangibleAssetSearchResponse> content = queryFactory
                .select(Projections.constructor(
                        TangibleAssetSearchResponse.class,
                        tangibleAssetItem.productName,
                        tangibleAsset.assetCode,
                        member.name,
                        member.memberNo,
                        tangibleAsset.tangibleAssetStatus,
                        department.name
                ))
                .from(tangibleAsset)
                .join(tangibleAsset.tangibleAssetItem, tangibleAssetItem)
                .join(tangibleAssetItem.tangibleAssetCategory, tangibleAssetCategory)
                .leftJoin(tangibleAsset.member, member)
                .leftJoin(tangibleAsset.department, department)
                .where(condition)
                .orderBy(tangibleAsset.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(tangibleAsset.count())
                .from(tangibleAsset)
                .join(tangibleAsset.tangibleAssetItem, tangibleAssetItem)
                .join(tangibleAssetItem.tangibleAssetCategory, tangibleAssetCategory)
                .leftJoin(tangibleAsset.member, member)
                .leftJoin(tangibleAsset.department, department)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
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
