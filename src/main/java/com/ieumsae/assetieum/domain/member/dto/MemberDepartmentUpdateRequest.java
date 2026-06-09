package com.ieumsae.assetieum.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberDepartmentUpdateRequest {

	@NotNull(message = "부서는 필수입니다.")
	private UUID departmentId;
}
