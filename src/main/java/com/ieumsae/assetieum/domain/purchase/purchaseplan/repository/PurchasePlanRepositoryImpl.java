package com.ieumsae.assetieum.domain.purchase.purchaseplan.repository;

import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanStatisticResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlan;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.QPurchasePlan.purchasePlan;
import static com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.QPurchasePlanItem.purchasePlanItem;

@Repository
@RequiredArgsConstructor
public class PurchasePlanRepositoryImpl implements PurchasePlanRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PurchasePlanResponse> search(
            UUID companyId,
            PurchaseRequestStatus status,
            UUID requesterId,
            String keyword,
            Pageable pageable
    ) {
        BooleanBuilder condition = buildCondition(companyId, status, requesterId, keyword);

        List<UUID> planIds = queryFactory
                .select(purchasePlan.id)
                .from(purchasePlan)
                .leftJoin(purchasePlanItem).on(purchasePlanItem.purchasePlan.eq(purchasePlan))
                .where(condition)
                .groupBy(purchasePlan.id)
                .orderBy(purchasePlan.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (planIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        Map<UUID, PurchasePlan> purchasePlanById = queryFactory
                .selectFrom(purchasePlan)
                .where(purchasePlan.id.in(planIds))
                .orderBy(purchasePlan.createdAt.desc())
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        PurchasePlan::getId,
                        plan -> plan,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<PurchasePlanResponse> content = planIds.stream()
                .map(purchasePlanById::get)
                .map(PurchasePlanResponse::from)
                .toList();

        Long total = queryFactory
                .select(purchasePlan.id.countDistinct())
                .from(purchasePlan)
                .leftJoin(purchasePlanItem).on(purchasePlanItem.purchasePlan.eq(purchasePlan))
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public PurchasePlanStatisticResponse getPurchasePlanStatistics(UUID companyId) {

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(purchasePlan.company.id.eq(companyId));

        NumberExpression<Long> approvalWaitingCount = statusCount(PurchaseRequestStatus.REQUESTED);
        NumberExpression<Long> orderedCount = statusCount(PurchaseRequestStatus.ORDERED);
        NumberExpression<Long> completedCount = statusCount(PurchaseRequestStatus.COMPLETED);

        Tuple result = queryFactory
                .select(
                        purchasePlan.count(),
                        approvalWaitingCount,
                        orderedCount,
                        completedCount
                )
                .from(purchasePlan)
                .where(condition)
                .fetchOne();

        if(result == null) {
            return PurchasePlanStatisticResponse.builder()
                    .totalCount(0L)
                    .approvalWaitingCount(0L)
                    .orderedCount(0L)
                    .completedCount(0L)
                    .build();
        }

        return PurchasePlanStatisticResponse.builder()
                .totalCount(toLong(result.get(purchasePlan.count())))
                .approvalWaitingCount(toLong(result.get(approvalWaitingCount)))
                .orderedCount(toLong(result.get(orderedCount)))
                .completedCount(toLong(result.get(completedCount)))
                .build();
    }

    private BooleanBuilder buildCondition(
            UUID companyId,
            PurchaseRequestStatus status,
            UUID requesterId,
            String keyword
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(purchasePlan.company.id.eq(companyId));
        condition.and(purchasePlan.deletedAt.isNull());

        if (status != null) {
            condition.and(purchasePlan.purchaseRequestStatus.eq(status));
        }

        if (requesterId != null) {
            condition.and(purchasePlan.requester.id.eq(requesterId));
        }

        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            condition.and(
                    purchasePlan.planNo.containsIgnoreCase(trimmedKeyword)
                            .or(purchasePlanItem.itemName.equalsIgnoreCase(trimmedKeyword))
                            .or(purchasePlanItem.itemName.containsIgnoreCase(trimmedKeyword))
            );
        }

        return condition;
    }

    private NumberExpression<Long> statusCount(PurchaseRequestStatus... statuses) {
        return new CaseBuilder()
                .when(purchasePlan.purchaseRequestStatus.in(statuses))
                .then(1L)
                .otherwise(0L)
                .sum();
    }

    private long toLong(Long value) {
        return value == null ? 0 : value;
    }

}
