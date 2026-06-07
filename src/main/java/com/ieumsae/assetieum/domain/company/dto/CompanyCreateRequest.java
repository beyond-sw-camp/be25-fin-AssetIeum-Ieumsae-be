package com.ieumsae.assetieum.domain.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanyCreateRequest {

	@NotBlank(message = "회사 코드는 필수입니다.")
	@Size(max = 100, message = "회사 코드는 100자 이하여야 합니다.")
	private String companyCode;
}
