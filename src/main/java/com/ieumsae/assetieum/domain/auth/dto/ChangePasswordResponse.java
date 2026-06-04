package com.ieumsae.assetieum.domain.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ChangePasswordResponse {

	private final UUID memberId;
	private final LocalDateTime updatedAt;

	public ChangePasswordResponse(UUID memberId, LocalDateTime updatedAt) {
		this.memberId = memberId;
		this.updatedAt = updatedAt;
	}
}
