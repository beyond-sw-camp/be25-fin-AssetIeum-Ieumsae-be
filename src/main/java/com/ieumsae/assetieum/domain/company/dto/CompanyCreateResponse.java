package com.ieumsae.assetieum.domain.company.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.company.entity.Company;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyCreateResponse {

	private final UUID companyId;
	private final String companyCode;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime createdAt;

	public static CompanyCreateResponse from(Company company) {
		return CompanyCreateResponse.builder()
			.companyId(company.getId())
			.companyCode(company.getCompanyCode())
			.createdAt(company.getCreatedAt())
			.build();
	}
}
