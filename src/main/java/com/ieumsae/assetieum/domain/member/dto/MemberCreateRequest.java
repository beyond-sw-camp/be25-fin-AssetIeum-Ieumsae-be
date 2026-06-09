package com.ieumsae.assetieum.domain.member.dto;

import com.ieumsae.assetieum.domain.member.type.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberCreateRequest {

	@NotBlank(message = "사번은 필수입니다.")
	@Size(max = 100, message = "사번은 100자 이하여야 합니다.")
	private String memberNo;

	@NotBlank(message = "이름은 필수입니다.")
	@Size(max = 100, message = "이름은 100자 이하여야 합니다.")
	private String name;

	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
	private String email;

	@NotNull(message = "부서는 필수입니다.")
	private UUID departmentId;

	@NotNull(message = "역할은 필수입니다.")
	private MemberRole role;
}
