package com.ieumsae.assetieum.domain.inspection.target.repository;

import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectorType;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.ieumsae.assetieum.domain.inspection.inspection.entity.QInspection.inspection;
import static com.ieumsae.assetieum.domain.inspection.target.entity.QInspectionTarget.inspectionTarget;
import static com.ieumsae.assetieum.domain.intangibleasset.asset.entity.QIntangibleAsset.intangibleAsset;
import static com.ieumsae.assetieum.domain.intangibleasset.category.entity.QIntangibleAssetCategory.intangibleAssetCategory;
import static com.ieumsae.assetieum.domain.intangibleasset.item.entity.QIntangibleAssetItem.intangibleAssetItem;
import static com.ieumsae.assetieum.domain.tangibleasset.asset.entity.QTangibleAsset.tangibleAsset;
import static com.ieumsae.assetieum.domain.tangibleasset.category.entity.QTangibleAssetCategory.tangibleAssetCategory;
import static com.ieumsae.assetieum.domain.tangibleasset.item.entity.QTangibleAssetItem.tangibleAssetItem;

@Repository
@RequiredArgsConstructor
public class InspectionTargetRepositoryImpl implements InspectionTargetRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<InspectionTarget> searchMyTargets(
            UUID companyId,
            UUID memberId,
            InspectionType inspectionType,
            InspectionStatus status,
            Boolean isResponded,
            Pageable pageable
    ) {
        BooleanBuilder condition = buildMyTargetCondition(companyId, memberId, inspectionType, status, isResponded);

        List<InspectionTarget> content = baseTargetQuery(inspectionType)
                .where(condition)
                .orderBy(inspection.startDate.desc(), inspectionTarget.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = countTargetQuery(inspectionType)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<InspectionTarget> searchInspectorTargets(
            UUID companyId,
            UUID inspectorId,
            InspectionType inspectionType,
            InspectionStatus status,
            Boolean isResponded,
            Pageable pageable
    ) {
        BooleanBuilder condition = buildInspectorTargetCondition(companyId, inspectorId, inspectionType, status, isResponded);

        List<InspectionTarget> content = baseTargetQuery(inspectionType)
                .where(condition)
                .orderBy(inspection.startDate.desc(), inspectionTarget.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = countTargetQuery(inspectionType)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildMyTargetCondition(
            UUID companyId,
            UUID memberId,
            InspectionType inspectionType,
            InspectionStatus status,
            Boolean isResponded
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(inspectionTarget.company.id.eq(companyId));
        condition.and(inspection.inspectionType.eq(inspectionType));
        condition.and(inspectionTarget.member.id.eq(memberId));

        if (status != null) {
            condition.and(inspection.inspectionStatus.eq(status));
        }

        if (isResponded != null) {
            condition.and(inspectionTarget.isResponded.eq(isResponded));
        }

        return condition;
    }

    private BooleanBuilder buildInspectorTargetCondition(
            UUID companyId,
            UUID inspectorId,
            InspectionType inspectionType,
            InspectionStatus status,
            Boolean isResponded
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(inspectionTarget.company.id.eq(companyId));
        condition.and(inspection.inspectionType.eq(inspectionType));
        condition.and(inspection.inspectorType.eq(InspectorType.ASSET_TEAM));
        condition.and(inspection.inspector.id.eq(inspectorId));

        if (status != null) {
            condition.and(inspection.inspectionStatus.eq(status));
        }

        if (isResponded != null) {
            condition.and(inspectionTarget.isResponded.eq(isResponded));
        }

        return condition;
    }

    private com.querydsl.jpa.impl.JPAQuery<InspectionTarget> baseTargetQuery(InspectionType inspectionType) {
        com.querydsl.jpa.impl.JPAQuery<InspectionTarget> query = queryFactory
                .selectFrom(inspectionTarget)
                .join(inspectionTarget.inspection, inspection).fetchJoin()
                .leftJoin(inspectionTarget.member).fetchJoin();

        if (inspectionType == InspectionType.TANGIBLE_ASSET) {
            return query
                    .join(inspectionTarget.tangibleAsset, tangibleAsset).fetchJoin()
                    .join(tangibleAsset.tangibleAssetItem, tangibleAssetItem).fetchJoin()
                    .join(tangibleAssetItem.tangibleAssetCategory, tangibleAssetCategory).fetchJoin();
        }

        return query
                .join(inspectionTarget.intangibleAsset, intangibleAsset).fetchJoin()
                .join(intangibleAsset.intangibleAssetItem, intangibleAssetItem).fetchJoin()
                .join(intangibleAssetItem.intangibleAssetCategory, intangibleAssetCategory).fetchJoin();
    }

    private com.querydsl.jpa.impl.JPAQuery<Long> countTargetQuery(InspectionType inspectionType) {
        com.querydsl.jpa.impl.JPAQuery<Long> query = queryFactory
                .select(inspectionTarget.count())
                .from(inspectionTarget)
                .join(inspectionTarget.inspection, inspection);

        if (inspectionType == InspectionType.TANGIBLE_ASSET) {
            return query.join(inspectionTarget.tangibleAsset, tangibleAsset);
        }

        return query.join(inspectionTarget.intangibleAsset, intangibleAsset);
    }
}
