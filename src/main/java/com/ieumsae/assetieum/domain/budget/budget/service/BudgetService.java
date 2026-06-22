package com.ieumsae.assetieum.domain.budget.budget.service;

import com.ieumsae.assetieum.domain.budget.budget.dto.BudgetResponse;
import com.ieumsae.assetieum.domain.budget.budget.dto.BudgetSearchRequest;
import com.ieumsae.assetieum.domain.budget.budget.entity.Budget;
import com.ieumsae.assetieum.domain.budget.budget.repository.BudgetRepository;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final CompanyRepository companyRepository;
    private final BudgetRepository budgetRepository;
    private final MemberRepository memberRepository;

    public PaginationResponse<BudgetResponse> getBudgets(
            BudgetSearchRequest request,
            UUID companyId
    ) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Page<Budget> budgetPage = request.getBudgetYear() == null
                ? budgetRepository.findAllByCompany_Id(companyId, request.toPageable())
                : budgetRepository.findAllByCompany_IdAndBudgetYear(
                        companyId,
                        request.getBudgetYear(),
                        request.toPageable()
                );

        return PaginationResponse.from(budgetPage.map(BudgetResponse::from));
    }

    public BudgetResponse getDepartmentBudget(
            Integer budgetYear,
            UUID departmentId,
            AuthenticatedMember authenticatedMember
    ) {
        companyRepository.findById(authenticatedMember.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Budget budget = budgetRepository.findAllByCompany_IdAndBudgetYearAndDepartment_Id(authenticatedMember.companyId(), budgetYear, departmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUDGET_NOT_FOUND));

        Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(authenticatedMember.id(), authenticatedMember.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        validateDepartmentBudgetReadable(member, budget);

        return BudgetResponse.from(budget);
    }

    private void validateDepartmentBudgetReadable(Member member, Budget budget) {
        if (member.getRole() == MemberRole.ADMIN) {
            return;
        }

        if (!member.getDepartment().getId().equals(budget.getDepartment().getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}
