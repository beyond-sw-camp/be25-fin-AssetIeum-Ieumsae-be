package com.ieumsae.assetieum.domain.department.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.department.entity.Department;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentTreeResponse {

	private final UUID departmentId;
	private final UUID parentDepartmentId;
	private final String name;
	private final long memberCount;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime createdAt;

	@Builder.Default
	private final List<DepartmentTreeResponse> children = new ArrayList<>();

	public static DepartmentTreeResponse from(Department department, long memberCount) {
		Department parentDepartment = department.getParentDepartment();

		return DepartmentTreeResponse.builder()
			.departmentId(department.getId())
			.parentDepartmentId(parentDepartment == null ? null : parentDepartment.getId())
			.name(department.getName())
			.memberCount(memberCount)
			.createdAt(department.getCreatedAt())
			.build();
	}

	public void addChild(DepartmentTreeResponse child) {
		this.children.add(child);
	}
}
