package com.ieumsae.assetieum.domain.company.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class
CompanyCreateResponse {

	private final UUID companyId;
	private final String companyCode;
	private final LocalDateTime createdAt;
}
