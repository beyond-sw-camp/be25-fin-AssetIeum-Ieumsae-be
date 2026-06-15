package com.ieumsae.assetieum.domain.budget.history.service;

import com.ieumsae.assetieum.domain.budget.history.dto.BudgetHistoryResponse;
import com.ieumsae.assetieum.domain.budget.history.dto.BudgetHistorySearchRequest;
import com.ieumsae.assetieum.domain.budget.history.repository.BudgetHistoryRepository;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetHistoryService {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final BudgetHistoryRepository budgetHistoryRepository;

    public PaginationResponse<BudgetHistoryResponse> getBudgetHistories(
            BudgetHistorySearchRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        if (request.getDepartmentId() != null) {
            departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getDepartmentId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
        }

        // 2. 예산 이력 목록 반환
        Page<BudgetHistoryResponse> budgetHistoryPage = budgetHistoryRepository.search(
                companyId,
                request.getDepartmentId(),
                request.getBudgetYear(),
                request.toPageable()
        );

        return PaginationResponse.from(budgetHistoryPage);
    }
}
