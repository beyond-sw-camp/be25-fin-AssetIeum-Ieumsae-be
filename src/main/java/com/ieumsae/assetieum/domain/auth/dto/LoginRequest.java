package com.ieumsae.assetieum.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

	@NotBlank(message = "사번은 필수입니다.")
	private String employeeNumber;

	@NotBlank(message = "비밀번호는 필수입니다.")
	private String password;

	public LoginRequest(String employeeNumber, String password) {
		this.employeeNumber = employeeNumber;
		this.password = password;
	}
}
