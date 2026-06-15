package com.ieumsae.assetieum.domain.budget.history.repository;

import com.ieumsae.assetieum.domain.budget.history.entity.BudgetHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetHistoryRepository extends JpaRepository<BudgetHistory, Long>, BudgetHistoryRepositoryCustom{
}
