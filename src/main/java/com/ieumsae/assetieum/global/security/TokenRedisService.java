package com.ieumsae.assetieum.global.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenRedisService {

	private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh:";
	private static final String ACCESS_BLACKLIST_KEY_PREFIX = "auth:blacklist:access:";

	private final StringRedisTemplate redisTemplate;

	public void saveRefreshToken(UUID memberId, String refreshToken, long expiresInSeconds) {
		redisTemplate.opsForValue().set(
			refreshTokenKey(memberId),
			// Redis에는 토큰 원문 대신 해시만 저장해 유출 시 피해를 줄인다.
			sha256(refreshToken),
			Duration.ofSeconds(expiresInSeconds)
		);
	}

	public boolean matchesRefreshToken(UUID memberId, String refreshToken) {
		String savedHash = redisTemplate.opsForValue().get(refreshTokenKey(memberId));
		return savedHash != null && savedHash.equals(sha256(refreshToken));
	}

	public void deleteRefreshToken(UUID memberId) {
		redisTemplate.delete(refreshTokenKey(memberId));
	}

	public void blacklistAccessToken(String accessToken, long remainingSeconds) {
		if (remainingSeconds <= 0) {
			return;
		}

		// 로그아웃된 Access Token은 남은 만료 시간 동안만 차단한다.
		redisTemplate.opsForValue().set(
			accessBlacklistKey(accessToken),
			"logout",
			Duration.ofSeconds(remainingSeconds)
		);
	}

	public boolean isAccessTokenBlacklisted(String accessToken) {
		Boolean exists = redisTemplate.hasKey(accessBlacklistKey(accessToken));
		return Boolean.TRUE.equals(exists);
	}

	private String refreshTokenKey(UUID memberId) {
		return REFRESH_TOKEN_KEY_PREFIX + memberId;
	}

	private String accessBlacklistKey(String accessToken) {
		return ACCESS_BLACKLIST_KEY_PREFIX + sha256(accessToken);
	}

	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
		}
	}
}
