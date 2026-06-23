package com.ieumsae.assetieum.domain.budget.budget.repository;

import com.ieumsae.assetieum.domain.budget.budget.entity.Budget;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    Page<Budget> findAllByCompany_Id(UUID companyId, Pageable pageable);

    Page<Budget> findAllByCompany_IdAndBudgetYear(UUID companyId, Integer budgetYear, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Budget> findByCompany_IdAndDepartment_IdAndBudgetYear(
            UUID companyId,
            UUID departmentId,
            Integer budgetYear
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Budget> findByCompany_IdAndDepartmentIsNullAndBudgetYear(
            UUID companyId,
            Integer budgetYear
    );

    Optional<Budget> findAllByCompany_IdAndBudgetYearAndDepartment_Id(
            UUID companyId,
            Integer budgetYear,
            UUID departmentId
    );
}
