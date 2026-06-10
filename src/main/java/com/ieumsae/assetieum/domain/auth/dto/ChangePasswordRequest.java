package com.ieumsae.assetieum.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordRequest {

	@NotBlank(message = "현재 비밀번호는 필수입니다.")
	private String currentPassword;

	@NotBlank(message = "새 비밀번호는 필수입니다.")
	@Size(min = 8, max = 20, message = "새 비밀번호는 8자 이상 20자 이하이어야 합니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
		message = "새 비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
	)
	private String newPassword;

	@NotBlank(message = "새 비밀번호 확인은 필수입니다.")
	private String confirmPassword;

	public ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {
		this.currentPassword = currentPassword;
		this.newPassword = newPassword;
		this.confirmPassword = confirmPassword;
	}
}
