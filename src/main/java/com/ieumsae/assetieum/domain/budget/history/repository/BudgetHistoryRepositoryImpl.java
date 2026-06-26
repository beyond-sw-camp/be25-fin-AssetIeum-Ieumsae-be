package com.ieumsae.assetieum.domain.budget.history.repository;

import com.ieumsae.assetieum.domain.budget.history.dto.BudgetHistoryResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.ieumsae.assetieum.domain.budget.budget.entity.QBudget.budget;
import static com.ieumsae.assetieum.domain.budget.history.entity.QBudgetHistory.budgetHistory;
import static com.ieumsae.assetieum.domain.department.entity.QDepartment.department;
import static com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.QPurchasePlan.purchasePlan;
import static com.ieumsae.assetieum.domain.ticket.common.entity.QTicket.ticket;


/**
 * 예산 이력 Repository 구현체
 * QueryDSL을 사용하여 동적 쿼리를 처리한다.
 */
@Repository
@RequiredArgsConstructor
public class BudgetHistoryRepositoryImpl implements BudgetHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 회사 기준 예산 이력 목록을 조회한다.
     * 부서 ID, 예산 년도를 동적으로 적용한다.
     */
    @Override
    public Page<BudgetHistoryResponse> search(
            UUID companyId,
            UUID departmentId,
            Integer budgetYear,
            Pageable pageable
    ) {

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(budgetHistory.company.id.eq(companyId));

        if (departmentId != null) {
            condition.and(budgetHistory.department.id.eq(departmentId));
        }

        if (budgetYear != null) {
            condition.and(budget.budgetYear.eq(budgetYear));
        }

        List<BudgetHistoryResponse> content = queryFactory
                .select(Projections.constructor(
                        BudgetHistoryResponse.class,
                        budgetHistory.id,
                        budgetHistory.department.id,
                        department.name,
                        budgetHistory.budget.id,
                        budget.budgetYear,
                        budgetHistory.ticket.id,
                        ticket.ticketNo,
                        budgetHistory.purchasePlan.id,
                        purchasePlan.planNo,
                        budgetHistory.historyType,
                        budgetHistory.amount,
                        budgetHistory.usedAmountBefore,
                        budgetHistory.usedAmountAfter,
                        budgetHistory.holdAmountBefore,
                        budgetHistory.holdAmountAfter,
                        budgetHistory.totalBudget,
                        budgetHistory.description,
                        budgetHistory.createdAt
                ))
                .from(budgetHistory)
                .join(budgetHistory.budget, budget)
                .leftJoin(budgetHistory.department, department)
                .leftJoin(budgetHistory.ticket, ticket)
                .leftJoin(budgetHistory.purchasePlan, purchasePlan)
                .where(condition)
                .orderBy(budgetHistory.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(budgetHistory.count())
                .from(budgetHistory)
                .join(budgetHistory.budget, budget)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
