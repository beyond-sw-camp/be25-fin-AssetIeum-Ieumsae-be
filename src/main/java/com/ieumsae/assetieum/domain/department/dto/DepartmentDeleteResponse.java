package com.ieumsae.assetieum.domain.department.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.department.entity.Department;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentDeleteResponse {

	private final UUID departmentId;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime deletedAt;

	public static DepartmentDeleteResponse from(Department department, LocalDateTime deletedAt) {
		return DepartmentDeleteResponse.builder()
			.departmentId(department.getId())
			.deletedAt(deletedAt)
			.build();
	}
}
