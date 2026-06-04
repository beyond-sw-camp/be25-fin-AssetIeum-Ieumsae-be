package com.ieumsae.assetieum.domain.company.service;

import com.ieumsae.assetieum.domain.company.dto.CompanyResponse;
import com.ieumsae.assetieum.domain.company.dto.CreateCompanyRequest;
import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

	private final CompanyRepository companyRepository;
	private final MemberRepository memberRepository;

	public CompanyService(CompanyRepository companyRepository, MemberRepository memberRepository) {
		this.companyRepository = companyRepository;
		this.memberRepository = memberRepository;
	}

	@Transactional
	public CompanyResponse createCompany(AuthenticatedMember authenticatedMember, CreateCompanyRequest request) {
		Member member = memberRepository.findById(authenticatedMember.id())
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		if (companyRepository.existsByCompanyCodeAndDeletedAtIsNull(request.getCompanyCode())) {
			throw new BusinessException(ErrorCode.COMPANY_ALREADY_EXISTS);
		}

		Company company = companyRepository.save(new Company(request.getCompanyCode()));
		return CompanyResponse.from(company);
	}
}
