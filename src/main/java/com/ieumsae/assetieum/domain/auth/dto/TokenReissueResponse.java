package com.ieumsae.assetieum.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenReissueResponse {

	private final String accessToken;
	private final String tokenType;
	private final long expiresIn;

	public static TokenReissueResponse from(String accessToken, long expiresIn) {
		return TokenReissueResponse.builder()
			.accessToken(accessToken)
			.tokenType("Bearer")
			.expiresIn(expiresIn)
			.build();
	}
}
