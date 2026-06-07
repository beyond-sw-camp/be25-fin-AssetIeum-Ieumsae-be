package com.ieumsae.assetieum.domain.department.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentDeleteResponse {

	private final UUID departmentId;
	private final LocalDateTime deletedAt;
}
