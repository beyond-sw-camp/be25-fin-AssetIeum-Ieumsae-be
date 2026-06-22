package com.ieumsae.assetieum.domain.member.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberOffboardingStartRequest {

	private LocalDateTime resignedAt;

	@Size(max = 255, message = "퇴사 사유는 255자 이하여야 합니다.")
	private String reason;
}
