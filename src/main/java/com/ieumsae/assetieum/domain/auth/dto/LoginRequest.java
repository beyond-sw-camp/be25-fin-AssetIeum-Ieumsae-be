package com.ieumsae.assetieum.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
	@NotBlank(message = "사번은 필수입니다.")
	String employeeNumber,

	@NotBlank(message = "비밀번호는 필수입니다.")
	String password
) {
}
