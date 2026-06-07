package com.ieumsae.assetieum.domain.department.dto;

import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentCreateResponse {

	private final UUID departmentId;
	private final UUID parentDepartmentId;
	private final UUID departmentManagerId;
	private final String departmentManagerName;
	private final String name;
	private final LocalDateTime createdAt;

	public static DepartmentCreateResponse from(Department department) {
		Department parentDepartment = department.getParentDepartment();
		Member departmentManager = department.getDepartmentManager();

		return DepartmentCreateResponse.builder()
			.departmentId(department.getId())
			.parentDepartmentId(parentDepartment == null ? null : parentDepartment.getId())
			.departmentManagerId(departmentManager == null ? null : departmentManager.getId())
			.departmentManagerName(departmentManager == null ? null : departmentManager.getName())
			.name(department.getName())
			.createdAt(department.getCreatedAt())
			.build();
	}
}
