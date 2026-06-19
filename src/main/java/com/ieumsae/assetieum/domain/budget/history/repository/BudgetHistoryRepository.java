package com.ieumsae.assetieum.domain.budget.history.repository;

import com.ieumsae.assetieum.domain.budget.history.entity.BudgetHistory;
import com.ieumsae.assetieum.domain.budget.history.type.BudgetHistoryType;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetHistoryRepository extends JpaRepository<BudgetHistory, Long>, BudgetHistoryRepositoryCustom{

    @Query("""
            select coalesce(sum(bh.amount), 0)
            from BudgetHistory bh
            where bh.company.id = :companyId
                and bh.ticket.id = :ticketId
                and bh.historyType = :historyType
            """)
    BigDecimal sumAmountByTicketAndHistoryType(
            @Param("companyId") UUID companyId,
            @Param("ticketId") UUID ticketId,
            @Param("historyType") BudgetHistoryType historyType
    );
}
