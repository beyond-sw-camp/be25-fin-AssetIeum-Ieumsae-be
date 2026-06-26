package com.ieumsae.assetieum.domain.company.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.company.entity.Company;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyDeleteResponse {

	private final UUID companyId;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime deletedAt;

	public static CompanyDeleteResponse from(Company company, LocalDateTime deletedAt) {
		return CompanyDeleteResponse.builder()
			.companyId(company.getId())
			.deletedAt(deletedAt)
			.build();
	}
}
