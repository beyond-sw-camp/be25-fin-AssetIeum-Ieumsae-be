package com.ieumsae.assetieum.domain.department.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DepartmentUpdateRequest {

	// 내부 제공 여부 플래그가 JSON 요청 필드로 노출되지 않도록 getter를 명시적으로 작성한다.
	private UUID parentDepartmentId;
	private boolean parentDepartmentIdProvided;

	@Size(max = 100, message = "부서명은 100자 이하여야 합니다.")
	private String name;
	private boolean nameProvided;

	private UUID departmentManagerId;
	private boolean departmentManagerIdProvided;

	public UUID getParentDepartmentId() {
		return parentDepartmentId;
	}

	public void setParentDepartmentId(UUID parentDepartmentId) {
		this.parentDepartmentId = parentDepartmentId;
		this.parentDepartmentIdProvided = true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.nameProvided = true;
	}

	public UUID getDepartmentManagerId() {
		return departmentManagerId;
	}

	public void setDepartmentManagerId(UUID departmentManagerId) {
		this.departmentManagerId = departmentManagerId;
		this.departmentManagerIdProvided = true;
	}

	public boolean parentDepartmentIdProvided() {
		return parentDepartmentIdProvided;
	}

	public boolean nameProvided() {
		return nameProvided;
	}

	public boolean departmentManagerIdProvided() {
		return departmentManagerIdProvided;
	}
}
