package com.ieumsae.assetieum.domain.department.controller;

import com.ieumsae.assetieum.domain.department.dto.DepartmentCreateRequest;
import com.ieumsae.assetieum.domain.department.dto.DepartmentCreateResponse;
import com.ieumsae.assetieum.domain.department.dto.DepartmentDeleteResponse;
import com.ieumsae.assetieum.domain.department.dto.DepartmentDetailResponse;
import com.ieumsae.assetieum.domain.department.dto.DepartmentListResponse;
import com.ieumsae.assetieum.domain.department.dto.DepartmentUpdateRequest;
import com.ieumsae.assetieum.domain.department.dto.DepartmentUpdateResponse;
import com.ieumsae.assetieum.domain.department.service.DepartmentService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
public class DepartmentController {

	private final DepartmentService departmentService;

	@GetMapping
	public ApiResponse<DepartmentListResponse> getDepartments(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		DepartmentListResponse response = departmentService.getDepartments(authenticatedMember);
		return ApiResponse.ok("부서 목록 조회에 성공했습니다.", response);
	}

	@GetMapping("/{departmentId}")
	public ApiResponse<DepartmentDetailResponse> getDepartment(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID departmentId
	) {
		DepartmentDetailResponse response = departmentService.getDepartment(authenticatedMember, departmentId);
		return ApiResponse.ok("부서 상세 조회에 성공했습니다.", response);
	}

	@PostMapping
	public ApiResponse<DepartmentCreateResponse> createDepartment(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody DepartmentCreateRequest request
	) {
		DepartmentCreateResponse response = departmentService.createDepartment(authenticatedMember, request);
		return ApiResponse.ok("부서 등록에 성공했습니다.", response);
	}

	@PatchMapping("/{departmentId}")
	public ApiResponse<DepartmentUpdateResponse> updateDepartment(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID departmentId,
		@Valid @RequestBody DepartmentUpdateRequest request
	) {
		DepartmentUpdateResponse response = departmentService.updateDepartment(
			authenticatedMember,
			departmentId,
			request
		);
		return ApiResponse.ok("부서 수정에 성공했습니다.", response);
	}

	@DeleteMapping("/{departmentId}")
	public ApiResponse<DepartmentDeleteResponse> deleteDepartment(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID departmentId
	) {
		DepartmentDeleteResponse response = departmentService.deleteDepartment(authenticatedMember, departmentId);
		return ApiResponse.ok("부서 삭제에 성공했습니다.", response);
	}
}
