package com.ieumsae.assetieum.domain.inspection.followup.repository;

import static com.ieumsae.assetieum.domain.inspection.followup.entity.QInspectionFollowUp.inspectionFollowUp;
import static com.ieumsae.assetieum.domain.inspection.inspection.entity.QInspection.inspection;
import static com.ieumsae.assetieum.domain.inspection.result.entity.QInspectionResult.inspectionResult;
import static com.ieumsae.assetieum.domain.inspection.target.entity.QInspectionTarget.inspectionTarget;
import static com.ieumsae.assetieum.domain.intangibleasset.asset.entity.QIntangibleAsset.intangibleAsset;
import static com.ieumsae.assetieum.domain.intangibleasset.item.entity.QIntangibleAssetItem.intangibleAssetItem;
import static com.ieumsae.assetieum.domain.tangibleasset.asset.entity.QTangibleAsset.tangibleAsset;
import static com.ieumsae.assetieum.domain.tangibleasset.item.entity.QTangibleAssetItem.tangibleAssetItem;

import com.ieumsae.assetieum.domain.inspection.followup.entity.InspectionFollowUp;
import com.ieumsae.assetieum.domain.inspection.followup.type.InspectionFollowUpStatus;
import com.ieumsae.assetieum.domain.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class InspectionFollowUpRepositoryImpl implements InspectionFollowUpRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<InspectionFollowUp> searchFollowUps(
            UUID companyId,
            UUID inspectorId,
            InspectionFollowUpStatus status,
            String keyword,
            Pageable pageable
    ) {
        BooleanBuilder condition = buildCondition(companyId, inspectorId, status, keyword);
        QMember inspector = new QMember("inspectionInspector");
        QMember targetMember = new QMember("targetMember");
        QMember tangibleAssetMember = new QMember("tangibleAssetMember");

        List<InspectionFollowUp> content = queryFactory
                .selectFrom(inspectionFollowUp)
                .distinct()
                .join(inspectionFollowUp.inspectionResult, inspectionResult).fetchJoin()
                .join(inspectionResult.inspection, inspection).fetchJoin()
                .join(inspection.inspector, inspector).fetchJoin()
                .join(inspectionResult.inspectionTarget, inspectionTarget).fetchJoin()
                .leftJoin(inspectionTarget.member, targetMember).fetchJoin()
                .leftJoin(inspectionTarget.tangibleAsset, tangibleAsset).fetchJoin()
                .leftJoin(tangibleAsset.member, tangibleAssetMember).fetchJoin()
                .leftJoin(tangibleAsset.tangibleAssetItem, tangibleAssetItem).fetchJoin()
                .leftJoin(inspectionTarget.intangibleAsset, intangibleAsset).fetchJoin()
                .leftJoin(intangibleAsset.intangibleAssetItem, intangibleAssetItem).fetchJoin()
                .where(condition)
                .orderBy(inspectionFollowUp.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(inspectionFollowUp.countDistinct())
                .from(inspectionFollowUp)
                .join(inspectionFollowUp.inspectionResult, inspectionResult)
                .join(inspectionResult.inspection, inspection)
                .join(inspectionResult.inspectionTarget, inspectionTarget)
                .leftJoin(inspectionTarget.tangibleAsset, tangibleAsset)
                .leftJoin(tangibleAsset.tangibleAssetItem, tangibleAssetItem)
                .leftJoin(inspectionTarget.intangibleAsset, intangibleAsset)
                .leftJoin(intangibleAsset.intangibleAssetItem, intangibleAssetItem)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildCondition(
            UUID companyId,
            UUID inspectorId,
            InspectionFollowUpStatus status,
            String keyword
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(inspectionFollowUp.company.id.eq(companyId));
        condition.and(inspection.inspector.id.eq(inspectorId));

        if (status != null) {
            condition.and(inspectionFollowUp.inspectionFollowUpStatus.eq(status));
        }

        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            condition.and(
                    tangibleAsset.assetCode.containsIgnoreCase(trimmedKeyword)
                            .or(tangibleAssetItem.productName.containsIgnoreCase(trimmedKeyword))
                            .or(intangibleAsset.assetCode.containsIgnoreCase(trimmedKeyword))
                            .or(intangibleAssetItem.productName.containsIgnoreCase(trimmedKeyword))
            );
        }

        return condition;
    }
}
