package com.ieumsae.assetieum.domain.company.dto;

import com.ieumsae.assetieum.domain.company.entity.Company;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CompanyResponse {

	private final UUID companyId;
	private final String companyCode;

	private CompanyResponse(UUID companyId, String companyCode) {
		this.companyId = companyId;
		this.companyCode = companyCode;
	}

	public static CompanyResponse from(Company company) {
		return new CompanyResponse(company.getId(), company.getCompanyCode());
	}
}
