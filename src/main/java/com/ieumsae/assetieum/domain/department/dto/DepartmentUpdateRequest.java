package com.ieumsae.assetieum.domain.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DepartmentUpdateRequest {

	@NotBlank(message = "부서명은 필수입니다.")
	@Size(max = 100, message = "부서명은 100자 이하여야 합니다.")
	private String name;

	private UUID departmentManagerId;
}
