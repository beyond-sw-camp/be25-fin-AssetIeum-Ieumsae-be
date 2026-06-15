package com.ieumsae.assetieum.domain.budget.history.repository;

import com.ieumsae.assetieum.domain.budget.history.dto.BudgetHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BudgetHistoryRepositoryCustom {
    Page<BudgetHistoryResponse> search(UUID companyId, UUID departmentId, Integer budgetYear, Pageable pageable);
}
