package com.ieumsae.assetieum.domain.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangePasswordResponse {

	private final UUID memberId;
	private final LocalDateTime updatedAt;
}
