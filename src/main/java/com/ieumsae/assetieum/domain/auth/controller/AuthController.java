package com.ieumsae.assetieum.domain.auth.controller;

import com.ieumsae.assetieum.domain.auth.dto.ChangePasswordRequest;
import com.ieumsae.assetieum.domain.auth.dto.ChangePasswordResponse;
import com.ieumsae.assetieum.domain.auth.dto.LoginRequest;
import com.ieumsae.assetieum.domain.auth.dto.LoginResponse;
import com.ieumsae.assetieum.domain.auth.dto.TokenReissueResponse;
import com.ieumsae.assetieum.domain.auth.service.AuthService;
import com.ieumsae.assetieum.domain.auth.service.AuthService.LoginResult;
import com.ieumsae.assetieum.domain.auth.service.AuthService.ReissueResult;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import com.ieumsae.assetieum.global.security.RefreshTokenCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private static final String BEARER_PREFIX = "Bearer ";

	private final AuthService authService;
	private final RefreshTokenCookieManager refreshTokenCookieManager;

	@PostMapping("/api/v1/auth/login")
	public ApiResponse<LoginResponse> login(
		@Valid @RequestBody LoginRequest request,
		HttpServletResponse response
	) {
		LoginResult result = authService.login(request);
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieManager.createCookieHeader(result.refreshToken()));
		return ApiResponse.ok("로그인에 성공했습니다.", result.response());
	}

	@PostMapping("/api/v1/auth/reissue")
	public ApiResponse<TokenReissueResponse> reissue(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String refreshToken = refreshTokenCookieManager.extractRefreshToken(request);
		ReissueResult result = authService.reissue(refreshToken);
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieManager.createCookieHeader(result.refreshToken()));
		return ApiResponse.ok("토큰 재발급에 성공했습니다.", result.response());
	}

	@PostMapping("/api/v1/auth/logout")
	public ApiResponse<Void> logout(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		authService.logout(authenticatedMember, resolveAccessToken(request));
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieManager.createExpiredCookieHeader());
		return ApiResponse.ok("로그아웃에 성공했습니다.");
	}

	@PatchMapping("/api/v1/members/me/password")
	public ApiResponse<ChangePasswordResponse> changePassword(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody ChangePasswordRequest request
	) {
		return ApiResponse.ok("비밀번호가 변경되었습니다.", authService.changePassword(authenticatedMember, request));
	}

	private String resolveAccessToken(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			return null;
		}
		return authorization.substring(BEARER_PREFIX.length());
	}
}
