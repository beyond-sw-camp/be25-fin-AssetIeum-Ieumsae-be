package com.ieumsae.assetieum.domain.member.controller;

import com.ieumsae.assetieum.domain.member.dto.MemberCreateRequest;
import com.ieumsae.assetieum.domain.member.dto.MemberCreateResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberDepartmentUpdateRequest;
import com.ieumsae.assetieum.domain.member.dto.MemberDepartmentUpdateResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberPageResponse;
import com.ieumsae.assetieum.domain.member.service.MemberService;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

	private final MemberService memberService;

	@GetMapping
	public ApiResponse<MemberPageResponse> getMembers(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) UUID departmentId,
		@RequestParam(required = false) MemberStatus status,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		MemberPageResponse response = memberService.getMembers(
			authenticatedMember,
			keyword,
			departmentId,
			status,
			pageable
		);
		return ApiResponse.ok("사원 목록 조회에 성공했습니다.", response);
	}

	@PostMapping
	public ApiResponse<MemberCreateResponse> createMember(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody MemberCreateRequest request
	) {
		MemberCreateResponse response = memberService.createMember(authenticatedMember, request);
		return ApiResponse.ok("사원 등록에 성공했습니다.", response);
	}

	@PatchMapping("/{memberId}/department")
	public ApiResponse<MemberDepartmentUpdateResponse> updateMemberDepartment(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID memberId,
		@Valid @RequestBody MemberDepartmentUpdateRequest request
	) {
		MemberDepartmentUpdateResponse response = memberService.updateMemberDepartment(
			authenticatedMember,
			memberId,
			request
		);
		return ApiResponse.ok("사원 소속 부서 변경에 성공했습니다.", response);
	}
}
