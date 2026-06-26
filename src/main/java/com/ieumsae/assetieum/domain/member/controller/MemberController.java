package com.ieumsae.assetieum.domain.member.controller;

import com.ieumsae.assetieum.domain.member.dto.MemberCreateRequest;
import com.ieumsae.assetieum.domain.member.dto.MemberCreateResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberDepartmentUpdateRequest;
import com.ieumsae.assetieum.domain.member.dto.MemberDepartmentUpdateResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberListItemResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberOffboardingCompleteResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberOffboardingStartRequest;
import com.ieumsae.assetieum.domain.member.dto.MemberOffboardingStartResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberOffboardingTargetsResponse;
import com.ieumsae.assetieum.domain.member.dto.MemberSearchRequest;
import com.ieumsae.assetieum.domain.member.service.MemberService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

	private final MemberService memberService;

	@PreAuthorize("isAuthenticated()")
	@GetMapping
	public ApiResponse<PaginationResponse<MemberListItemResponse>> getMembers(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute MemberSearchRequest request
	) {
		PaginationResponse<MemberListItemResponse> response = memberService.getMembers(
			authenticatedMember,
			request
		);
		return ApiResponse.ok("사원 목록 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public ApiResponse<MemberCreateResponse> createMember(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody MemberCreateRequest request
	) {
		MemberCreateResponse response = memberService.createMember(authenticatedMember, request);
		return ApiResponse.ok("사원 등록에 성공했습니다.", response);
	}

	@PreAuthorize("hasRole('ADMIN')")
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

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{memberId}/offboarding-targets")
	public ApiResponse<MemberOffboardingTargetsResponse> getOffboardingTargets(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID memberId
	) {
		MemberOffboardingTargetsResponse response = memberService.getOffboardingTargets(
			authenticatedMember,
			memberId
		);
		return ApiResponse.ok("퇴사 처리 대상 조회에 성공했습니다.", response);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{memberId}/offboarding/start")
	public ApiResponse<MemberOffboardingStartResponse> startOffboarding(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID memberId,
		@Valid @RequestBody MemberOffboardingStartRequest request
	) {
		MemberOffboardingStartResponse response = memberService.startOffboarding(
			authenticatedMember,
			memberId,
			request
		);
		return ApiResponse.ok("퇴사 처리를 시작했습니다.", response);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{memberId}/offboarding/complete")
	public ApiResponse<MemberOffboardingCompleteResponse> completeOffboarding(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID memberId
	) {
		MemberOffboardingCompleteResponse response = memberService.completeOffboarding(
			authenticatedMember,
			memberId
		);
		return ApiResponse.ok("퇴사 처리가 완료되었습니다.", response);
	}
}
