package com.ieumsae.assetieum.domain.inspection.inspection.repository;

import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.result.entity.InspectionResult;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import com.querydsl.core.BooleanBuilder;
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
}
