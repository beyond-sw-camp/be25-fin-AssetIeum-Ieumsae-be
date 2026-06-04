package com.ieumsae.assetieum.domain.auth.controller;

import com.ieumsae.assetieum.domain.auth.dto.ChangePasswordRequest;
import com.ieumsae.assetieum.domain.auth.dto.ChangePasswordResponse;
import com.ieumsae.assetieum.domain.auth.dto.LoginRequest;
import com.ieumsae.assetieum.domain.auth.dto.LoginResponse;
import com.ieumsae.assetieum.domain.auth.service.AuthService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/api/v1/auth/login")
	public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ApiResponse.ok("로그인에 성공했습니다.", authService.login(request));
	}

	@PatchMapping("/api/v1/members/me/password")
	public ApiResponse<ChangePasswordResponse> changePassword(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody ChangePasswordRequest request
	) {
		return ApiResponse.ok("비밀번호가 변경되었습니다.", authService.changePassword(authenticatedMember, request));
	}
}
