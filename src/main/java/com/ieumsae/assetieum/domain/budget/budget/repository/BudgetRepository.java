package com.ieumsae.assetieum.domain.budget.budget.repository;

import com.ieumsae.assetieum.domain.budget.budget.entity.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    Page<Budget> findAllByCompany_Id(UUID companyId, Pageable pageable);

    Page<Budget> findAllByCompany_IdAndBudgetYear(UUID companyId, Integer budgetYear, Pageable pageable);
}
