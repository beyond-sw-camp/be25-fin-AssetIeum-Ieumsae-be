package com.ieumsae.assetieum.global.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieManager {

	public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
	private static final String AUTH_COOKIE_PATH = "/api/v1/auth";

	private final JwtProvider jwtProvider;
	private final RefreshTokenCookieProperties properties;

	public String createCookieHeader(String refreshToken) {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
			// Refresh Token은 자바스크립트에서 읽지 못하도록 HttpOnly 쿠키로만 전달한다.
			.httpOnly(true)
			.secure(properties.isSecure())
			.sameSite(properties.getSameSite())
			.path(AUTH_COOKIE_PATH)
			.maxAge(Duration.ofSeconds(jwtProvider.getRefreshTokenExpiresInSeconds()))
			.build()
			.toString();
	}

	public String createExpiredCookieHeader() {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
			// 로그아웃 시 브라우저에 남은 Refresh Token 쿠키를 즉시 만료시킨다.
			.httpOnly(true)
			.secure(properties.isSecure())
			.sameSite(properties.getSameSite())
			.path(AUTH_COOKIE_PATH)
			.maxAge(Duration.ZERO)
			.build()
			.toString();
	}

	public String extractRefreshToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}

		for (Cookie cookie : cookies) {
			if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
