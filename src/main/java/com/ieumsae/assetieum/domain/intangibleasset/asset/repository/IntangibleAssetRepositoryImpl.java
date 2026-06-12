package com.ieumsae.assetieum.domain.intangibleasset.asset.repository;

import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.CaseBuilder;
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

import com.ieumsae.assetieum.domain.department.entity.QDepartment;
import com.ieumsae.assetieum.domain.member.entity.QMember;
import static com.ieumsae.assetieum.domain.department.entity.QDepartment.department;
import static com.ieumsae.assetieum.domain.intangibleasset.asset.entity.QIntangibleAsset.intangibleAsset;
import static com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.QAssignment.assignment;
import static com.ieumsae.assetieum.domain.intangibleasset.category.entity.QIntangibleAssetCategory.intangibleAssetCategory;
import static com.ieumsae.assetieum.domain.intangibleasset.item.entity.QIntangibleAssetItem.intangibleAssetItem;
import static com.ieumsae.assetieum.domain.member.entity.QMember.member;

/**
 * 무형자산 Repository 구현체
 * QueryDSL을 사용하여 동적 쿼리를 처리한다.
 */
@Repository
@RequiredArgsConstructor
public class IntangibleAssetRepositoryImpl implements IntangibleAssetRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final IntangibleAssetCategoryRepository categoryRepository;

    /**
     * 회사 기준 무형자산 목록을 조회한다.
     * 카테고리, 품목 ID, 상태, 키워드, 현재 사용자, 부서 조건을 동적으로 적용한다.
     */
    @Override
    public Page<IntangibleAssetSearchResponse> search(
            UUID companyId,
            UUID categoryId,
            IntangibleAssetStatus status,
            String keyword,
            UUID currentUserId,
            UUID departmentId,
            Pageable pageable
    ) {
        QMember assignedMember = new QMember("assignedMember");
        QDepartment assignedDepartment = new QDepartment("assignedDepartment");

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(intangibleAsset.company.id.eq(companyId));

        List<UUID> categoryIds = getCategoryIds(categoryId, companyId);
        if(categoryIds != null && !categoryIds.isEmpty()) {
            condition.and(intangibleAssetCategory.id.in(categoryIds));
        }

        if(status != null) {
            condition.and(intangibleAsset.intangibleAssetStatus.eq(status));
        }

        if(keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            condition.and(
                    intangibleAsset.licenseCode.containsIgnoreCase(trimmedKeyword)
                            .or(intangibleAsset.purchaseVendor.containsIgnoreCase(trimmedKeyword))
                            .or(intangibleAsset.assetCode.containsIgnoreCase(trimmedKeyword))
                            .or(intangibleAssetItem.productName.containsIgnoreCase(trimmedKeyword))
                            .or(intangibleAssetItem.provider.containsIgnoreCase(trimmedKeyword))
            );
        }

        if (currentUserId != null) {
            condition.and(
                    assignedMember.id.eq(currentUserId)
                            .or(assignment.id.isNull().and(member.id.eq(currentUserId)))
            );
        }

        if (departmentId != null) {
            condition.and(
                    assignedDepartment.id.eq(departmentId)
                            .or(assignment.id.isNull().and(department.id.eq(departmentId)))
            );
        }

        List<IntangibleAssetSearchResponse> content = queryFactory
                .select(Projections.constructor(
                        IntangibleAssetSearchResponse.class,
                        intangibleAsset.id,
                        intangibleAssetItem.productName,
                        intangibleAsset.assetCode,
                        new CaseBuilder()
                                .when(assignment.id.isNotNull())
                                .then(assignedMember.name)
                                .otherwise(member.name),
                        new CaseBuilder()
                                .when(assignment.id.isNotNull())
                                .then(assignedMember.memberNo)
                                .otherwise(member.memberNo),
                        intangibleAsset.intangibleAssetStatus,
                        new CaseBuilder()
                                .when(assignment.id.isNotNull())
                                .then(assignedDepartment.name)
                                .otherwise(department.name)
                ))
                .from(intangibleAsset)
                .join(intangibleAsset.intangibleAssetItem, intangibleAssetItem)
                .join(intangibleAssetItem.intangibleAssetCategory, intangibleAssetCategory)
                .leftJoin(intangibleAsset.member, member)
                .leftJoin(intangibleAsset.department, department)
                .leftJoin(assignment).on(
                        assignment.intangibleAsset.id.eq(intangibleAsset.id),
                        assignment.assignmentStatus.eq(AssignmentStatus.ACTIVE)
                )
                .leftJoin(assignment.member, assignedMember)
                .leftJoin(assignment.department, assignedDepartment)
                .where(condition)
                .orderBy(intangibleAsset.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(intangibleAsset.count())
                .from(intangibleAsset)
                .join(intangibleAsset.intangibleAssetItem, intangibleAssetItem)
                .join(intangibleAssetItem.intangibleAssetCategory, intangibleAssetCategory)
                .leftJoin(intangibleAsset.member, member)
                .leftJoin(intangibleAsset.department, department)
                .leftJoin(assignment).on(
                        assignment.intangibleAsset.id.eq(intangibleAsset.id),
                        assignment.assignmentStatus.eq(AssignmentStatus.ACTIVE)
                )
                .leftJoin(assignment.member, assignedMember)
                .leftJoin(assignment.department, assignedDepartment)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);

    }

    private List<UUID> getCategoryIds(UUID categoryId, UUID companyId) {
        if(categoryId == null) {
            return null;
        }

        List<UUID> categoryIds = new ArrayList<>(
                categoryRepository.findAllDescendantIds(categoryId, companyId)
        );

        categoryIds.add(categoryId);

        return categoryIds;
    }
}
