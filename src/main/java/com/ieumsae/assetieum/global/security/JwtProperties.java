package com.ieumsae.assetieum.global.security;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

	private String secret;
	private String issuer;
	private String audience;
	private long accessTokenExpirationMinutes;
	private long refreshTokenExpirationDays;

	@PostConstruct
	void validate() {
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 64) {
			throw new IllegalStateException("auth.jwt.secret must be at least 64 bytes.");
		}
		if (issuer == null || issuer.isBlank()) {
			throw new IllegalStateException("auth.jwt.issuer is required.");
		}
		if (audience == null || audience.isBlank()) {
			throw new IllegalStateException("auth.jwt.audience is required.");
		}
		if (accessTokenExpirationMinutes <= 0) {
			throw new IllegalStateException("auth.jwt.access-token-expiration-minutes must be positive.");
		}
		if (refreshTokenExpirationDays <= 0) {
			throw new IllegalStateException("auth.jwt.refresh-token-expiration-days must be positive.");
		}
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getAudience() {
		return audience;
	}

	public void setAudience(String audience) {
		this.audience = audience;
	}

	public long getAccessTokenExpirationMinutes() {
		return accessTokenExpirationMinutes;
	}

	public void setAccessTokenExpirationMinutes(long accessTokenExpirationMinutes) {
		this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
	}

	public long getRefreshTokenExpirationDays() {
		return refreshTokenExpirationDays;
	}

	public void setRefreshTokenExpirationDays(long refreshTokenExpirationDays) {
		this.refreshTokenExpirationDays = refreshTokenExpirationDays;
	}
}
