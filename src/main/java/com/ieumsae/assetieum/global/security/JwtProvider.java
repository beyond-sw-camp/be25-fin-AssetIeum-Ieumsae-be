package com.ieumsae.assetieum.global.security;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

	private static final String EMPLOYEE_NUMBER_CLAIM = "employeeNumber";
	private static final String ROLE_CLAIM = "role";
	private static final String TOKEN_TYPE_CLAIM = "tokenType";
	private static final String ACCESS_TOKEN_TYPE = "access";

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(Member member) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(getAccessTokenExpiresInSeconds());

		return Jwts.builder()
			.issuer(jwtProperties.getIssuer())
			.audience()
			.add(jwtProperties.getAudience())
			.and()
			.subject(member.getId().toString())
			.claim(EMPLOYEE_NUMBER_CLAIM, member.getEmployeeNumber())
			.claim(ROLE_CLAIM, member.getRole().name())
			.claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.signWith(secretKey, Jwts.SIG.HS256)
			.compact();
	}

	public AuthenticatedMember parseAccessToken(String token) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.requireIssuer(jwtProperties.getIssuer())
				.requireAudience(jwtProperties.getAudience())
				.build()
				.parseSignedClaims(token)
				.getPayload();

			if (!ACCESS_TOKEN_TYPE.equals(readStringClaim(claims, TOKEN_TYPE_CLAIM))) {
				throw new BusinessException(ErrorCode.INVALID_TOKEN);
			}

			return new AuthenticatedMember(
				UUID.fromString(claims.getSubject()),
				readStringClaim(claims, EMPLOYEE_NUMBER_CLAIM),
				MemberRole.valueOf(readStringClaim(claims, ROLE_CLAIM))
			);
		} catch (JwtException | IllegalArgumentException exception) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}
	}

	public long getAccessTokenExpiresInSeconds() {
		return jwtProperties.getAccessTokenExpirationMinutes() * 60;
	}

	private String readStringClaim(Claims claims, String name) {
		String value = claims.get(name, String.class);
		if (value == null || value.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}
		return value;
	}
}
