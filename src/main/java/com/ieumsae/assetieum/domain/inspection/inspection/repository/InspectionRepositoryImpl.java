package com.ieumsae.assetieum.domain.inspection.inspection.repository;

import com.ieumsae.assetieum.domain.inspection.followup.type.InspectionFollowUpStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.dto.InspectionStatisticsResponse;
import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.result.entity.InspectionResult;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ieumsae.assetieum.domain.department.entity.QDepartment.department;
import static com.ieumsae.assetieum.domain.inspection.followup.entity.QInspectionFollowUp.inspectionFollowUp;
import static com.ieumsae.assetieum.domain.inspection.inspection.entity.QInspection.inspection;
import static com.ieumsae.assetieum.domain.inspection.result.entity.QInspectionResult.inspectionResult;
import static com.ieumsae.assetieum.domain.inspection.target.entity.QInspectionTarget.inspectionTarget;
import static com.ieumsae.assetieum.domain.intangibleasset.asset.entity.QIntangibleAsset.intangibleAsset;
import static com.ieumsae.assetieum.domain.intangibleasset.category.entity.QIntangibleAssetCategory.intangibleAssetCategory;
import static com.ieumsae.assetieum.domain.intangibleasset.item.entity.QIntangibleAssetItem.intangibleAssetItem;
import static com.ieumsae.assetieum.domain.member.entity.QMember.member;
import static com.ieumsae.assetieum.domain.tangibleasset.asset.entity.QTangibleAsset.tangibleAsset;
import static com.ieumsae.assetieum.domain.tangibleasset.category.entity.QTangibleAssetCategory.tangibleAssetCategory;
import static com.ieumsae.assetieum.domain.tangibleasset.item.entity.QTangibleAssetItem.tangibleAssetItem;

@Repository
@RequiredArgsConstructor
public class InspectionRepositoryImpl implements InspectionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Inspection> search(
            UUID companyId,
            InspectionType inspectionType,
            InspectionStatus status,
            UUID inspectorId,
            Pageable pageable
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(inspection.company.id.eq(companyId));
        condition.and(inspection.inspectionType.eq(inspectionType));

        if (status != null) {
            condition.and(inspection.inspectionStatus.eq(status));
        }

        if (inspectorId != null) {
            condition.and(inspection.inspector.id.eq(inspectorId));
        }

        List<Inspection> content = queryFactory
                .selectFrom(inspection)
                .join(inspection.inspector, member).fetchJoin()
                .leftJoin(inspection.targetDepartment, department).fetchJoin()
                .where(condition)
                .orderBy(inspection.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(inspection.count())
                .from(inspection)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Optional<Inspection> findDetailByIdAndCompanyIdAndInspectionType(
            UUID inspectionId,
            UUID companyId,
            InspectionType inspectionType
    ) {
        Inspection result = queryFactory
                .selectFrom(inspection)
                .join(inspection.inspector, member).fetchJoin()
                .leftJoin(inspection.targetDepartment, department).fetchJoin()
                .where(
                        inspection.id.eq(inspectionId),
                        inspection.company.id.eq(companyId),
                        inspection.inspectionType.eq(inspectionType)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<InspectionTarget> findTargetsWithAssets(UUID inspectionId, UUID companyId) {
        return queryFactory
                .selectFrom(inspectionTarget)
                .leftJoin(inspectionTarget.tangibleAsset, tangibleAsset).fetchJoin()
                .leftJoin(tangibleAsset.tangibleAssetItem, tangibleAssetItem).fetchJoin()
                .leftJoin(tangibleAssetItem.tangibleAssetCategory, tangibleAssetCategory).fetchJoin()
                .leftJoin(inspectionTarget.intangibleAsset, intangibleAsset).fetchJoin()
                .leftJoin(intangibleAsset.intangibleAssetItem, intangibleAssetItem).fetchJoin()
                .leftJoin(intangibleAssetItem.intangibleAssetCategory, intangibleAssetCategory).fetchJoin()
                .where(
                        inspectionTarget.inspection.id.eq(inspectionId),
                        inspectionTarget.company.id.eq(companyId)
                )
                .fetch();
    }

    @Override
    public List<InspectionResult> findResults(UUID inspectionId, UUID companyId) {
        return queryFactory
                .selectFrom(inspectionResult)
                .join(inspectionResult.inspectionTarget).fetchJoin()
                .where(
                        inspectionResult.inspection.id.eq(inspectionId),
                        inspectionResult.company.id.eq(companyId)
                )
                .fetch();
    }

    @Override
    public InspectionStatisticsResponse getInspectionStatistics(UUID companyId, InspectionType inspectionType) {
        NumberExpression<Long> readyCount = inspectionStatusCount(InspectionStatus.READY);
        NumberExpression<Long> inProgressCount = inspectionStatusCount(InspectionStatus.IN_PROGRESS);
        NumberExpression<Long> completedCount = inspectionStatusCount(InspectionStatus.COMPLETED, InspectionStatus.CLOSED);

        Tuple inspectionStatusCounts = queryFactory
                .select(
                        inspection.count(),
                        readyCount,
                        inProgressCount,
                        completedCount
                )
                .from(inspection)
                .where(
                        inspection.company.id.eq(companyId),
                        inspection.inspectionType.eq(inspectionType),
                        inspection.inspectionStatus.in(
                                InspectionStatus.READY,
                                InspectionStatus.IN_PROGRESS,
                                InspectionStatus.COMPLETED,
                                InspectionStatus.CLOSED
                        )
                )
                .fetchOne();

        long totalInspectionCount = inspectionStatusCounts == null
                ? 0
                : toLong(inspectionStatusCounts.get(inspection.count()));
        long readyInspectionCount = inspectionStatusCounts == null
                ? 0
                : toLong(inspectionStatusCounts.get(readyCount));
        long inProgressInspectionCount = inspectionStatusCounts == null
                ? 0
                : toLong(inspectionStatusCounts.get(inProgressCount));
        long completedInspectionCount = inspectionStatusCounts == null
                ? 0
                : toLong(inspectionStatusCounts.get(completedCount));

        long inProgressTargetAssetCount = countTargetAssetsByInspectionStatus(
                companyId,
                inspectionType,
                InspectionStatus.IN_PROGRESS
        );
        long completedTargetAssetCount = countTargetAssetsByInspectionStatus(
                companyId,
                inspectionType,
                InspectionStatus.COMPLETED
        );

        return InspectionStatisticsResponse.builder()
                .totalInspectionCount(totalInspectionCount)
                .readyInspectionCount(readyInspectionCount)
                .inProgressInspectionCount(inProgressInspectionCount)
                .completedInspectionCount(completedInspectionCount)
                .inProgressTargetAssetCount(inProgressTargetAssetCount)
                .completedTargetAssetCount(completedTargetAssetCount)
                .unprocessedAssetCount(countUnprocessedAssets(companyId, inspectionType))
                .followUpInProgressAssetCount(countFollowUpInProgressAssets(companyId, inspectionType))
                .followUpCompletedAssetCount(countFollowUpCompletedAssets(companyId, inspectionType))
                .build();
    }

    private NumberExpression<Long> inspectionStatusCount(InspectionStatus... statuses) {
        return new CaseBuilder()
                .when(inspection.inspectionStatus.in(statuses))
                .then(1L)
                .otherwise(0L)
                .sum();
    }

    private long countTargetAssetsByInspectionStatus(
            UUID companyId,
            InspectionType inspectionType,
            InspectionStatus... statuses
    ) {
        Long count = queryFactory
                .select(inspectionTarget.count())
                .from(inspectionTarget)
                .join(inspectionTarget.inspection, inspection)
                .where(
                        inspectionTarget.company.id.eq(companyId),
                        inspection.inspectionType.eq(inspectionType),
                        inspection.inspectionStatus.in(statuses)
                )
                .fetchOne();

        return toLong(count);
    }

    private long countUnprocessedAssets(UUID companyId, InspectionType inspectionType) {
        Long count = queryFactory
                .select(inspectionTarget.count())
                .from(inspectionTarget)
                .join(inspectionTarget.inspection, inspection)
                .leftJoin(inspectionResult).on(inspectionResult.inspectionTarget.eq(inspectionTarget))
                .where(
                        inspectionTarget.company.id.eq(companyId),
                        inspection.inspectionType.eq(inspectionType),
                        inspection.inspectionStatus.eq(InspectionStatus.IN_PROGRESS),
                        inspectionResult.id.isNull()
                )
                .fetchOne();

        return toLong(count);
    }

    private long countFollowUpInProgressAssets(UUID companyId, InspectionType inspectionType) {
        Long count = queryFactory
                .select(inspectionResult.id.countDistinct())
                .from(inspectionResult)
                .join(inspectionResult.inspection, inspection)
                .leftJoin(inspectionFollowUp).on(inspectionFollowUp.inspectionResult.eq(inspectionResult))
                .where(
                        inspectionResult.company.id.eq(companyId),
                        inspection.inspectionType.eq(inspectionType),
                        inspectionResult.followUpRequests.isTrue(),
                        inspectionFollowUp.id.isNull()
                )
                .fetchOne();

        return toLong(count);
    }

    private long countFollowUpCompletedAssets(UUID companyId, InspectionType inspectionType) {
        Long count = queryFactory
                .select(inspectionResult.id.countDistinct())
                .from(inspectionResult)
                .join(inspectionResult.inspection, inspection)
                .join(inspectionFollowUp).on(inspectionFollowUp.inspectionResult.eq(inspectionResult))
                .where(
                        inspectionResult.company.id.eq(companyId),
                        inspection.inspectionType.eq(inspectionType),
                        inspectionResult.followUpRequests.isTrue(),
                        inspectionFollowUp.inspectionFollowUpStatus.eq(InspectionFollowUpStatus.COMPLETED)
                )
                .fetchOne();

        return toLong(count);
    }

    private long toLong(Long value) {
        return value == null ? 0 : value;
    }
}
