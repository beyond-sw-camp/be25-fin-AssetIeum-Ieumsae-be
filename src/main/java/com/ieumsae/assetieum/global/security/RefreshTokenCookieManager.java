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
