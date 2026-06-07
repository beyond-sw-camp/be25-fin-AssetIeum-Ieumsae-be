package com.ieumsae.assetieum.domain.company.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyDeleteResponse {

	private final UUID companyId;
	private final LocalDateTime deletedAt;
}
