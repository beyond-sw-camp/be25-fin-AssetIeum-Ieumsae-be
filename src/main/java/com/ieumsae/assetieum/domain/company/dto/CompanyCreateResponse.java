package com.ieumsae.assetieum.domain.company.dto;

import com.ieumsae.assetieum.domain.company.entity.Company;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CompanyCreateResponse {

	private final UUID companyId;
	private final String companyCode;
	private final LocalDateTime createdAt;

	public static CompanyCreateResponse from(Company company) {
		return CompanyCreateResponse.builder()
			.companyId(company.getId())
			.companyCode(company.getCompanyCode())
			.createdAt(company.getCreatedAt())
			.build();
	}
}
