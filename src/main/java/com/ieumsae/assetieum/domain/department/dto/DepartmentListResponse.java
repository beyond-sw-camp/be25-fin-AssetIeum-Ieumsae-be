package com.ieumsae.assetieum.domain.department.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentListResponse {

	private final List<DepartmentTreeResponse> content;

	public static DepartmentListResponse from(List<DepartmentTreeResponse> content) {
		return DepartmentListResponse.builder()
			.content(content)
			.build();
	}
}
