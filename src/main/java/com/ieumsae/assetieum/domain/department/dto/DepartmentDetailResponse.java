package com.ieumsae.assetieum.domain.department.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentDetailResponse {

	private final UUID departmentId;
	private final UUID parentDepartmentId;
	private final String parentDepartmentName;
	private final String name;
	private final UUID departmentManagerId;
	private final String departmentManagerName;
	private final long memberCount;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime createdAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime updatedAt;

	public static DepartmentDetailResponse from(Department department, long memberCount) {
		Department parentDepartment = department.getParentDepartment();
		Member departmentManager = department.getDepartmentManager();

		return DepartmentDetailResponse.builder()
			.departmentId(department.getId())
			.parentDepartmentId(parentDepartment == null ? null : parentDepartment.getId())
			.parentDepartmentName(parentDepartment == null ? null : parentDepartment.getName())
			.name(department.getName())
			.departmentManagerId(departmentManager == null ? null : departmentManager.getId())
			.departmentManagerName(departmentManager == null ? null : departmentManager.getName())
			.memberCount(memberCount)
			.createdAt(department.getCreatedAt())
			.updatedAt(department.getUpdatedAt())
			.build();
	}
}
