package com.ieumsae.assetieum.domain.company.service;

import com.ieumsae.assetieum.domain.company.dto.CompanyCreateRequest;
import com.ieumsae.assetieum.domain.company.dto.CompanyCreateResponse;
import com.ieumsae.assetieum.domain.company.dto.CompanyDeleteResponse;
import com.ieumsae.assetieum.domain.company.dto.CompanySearchRequest;
import com.ieumsae.assetieum.domain.company.dto.CompanySearchResponse;
import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.purchase.purchasepolicy.entity.PurchasePolicy;
import com.ieumsae.assetieum.domain.purchase.purchasepolicy.repository.PurchasePolicyRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

	private final CompanyRepository companyRepository;
	private final MemberRepository memberRepository;
	private final PurchasePolicyRepository purchasePolicyRepository;

	@Transactional
	public CompanyCreateResponse createCompany(
		AuthenticatedMember authenticatedMember,
		CompanyCreateRequest request
	) {
		validateSuperAdmin(authenticatedMember);

		if (companyRepository.existsByCompanyCode(request.getCompanyCode())
				|| companyRepository.existsByCompanyName(request.getCompanyName())) {
			throw new BusinessException(ErrorCode.COMPANY_ALREADY_EXISTS);
		}

		Company company = companyRepository.save(Company.builder()
			.companyCode(request.getCompanyCode())
			.companyName(request.getCompanyName())
			.build());

		purchasePolicyRepository.save(PurchasePolicy.builder()
			.company(company)
			.build());

		return CompanyCreateResponse.from(company);
	}

	@Transactional
	public CompanyDeleteResponse deleteCompany(
		AuthenticatedMember authenticatedMember,
		UUID companyId
	) {
		validateSuperAdmin(authenticatedMember);

		Company company = findActiveCompany(companyId);
		LocalDateTime deletedAt = company.delete();

		return CompanyDeleteResponse.from(company, deletedAt);
	}

	private void validateSuperAdmin(AuthenticatedMember authenticatedMember) {
		Member member = memberRepository.findById(authenticatedMember.id())
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		if (member.getRole() != MemberRole.SUPER_ADMIN) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private Company findActiveCompany(UUID companyId) {
		return companyRepository.findByIdAndDeletedAtIsNull(companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
	}

    public PaginationResponse<CompanySearchResponse> getCompanies(
			CompanySearchRequest request
	) {

		// 1. 페이징 처리 및 필터링 후 회사 목록 반환
		Page<CompanySearchResponse> companyPage = companyRepository.search(
				request.getKeyword(),
				request.toPageable()
		);

		return PaginationResponse.from(companyPage);
    }
}
