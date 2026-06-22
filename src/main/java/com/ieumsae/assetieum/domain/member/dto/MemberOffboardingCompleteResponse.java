package com.ieumsae.assetieum.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"memberId",
	"memberName",
	"memberStatus",
	"completedAt"
})
public class MemberOffboardingCompleteResponse {

	private final UUID memberId;
	private final String memberName;
	private final MemberStatus memberStatus;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime completedAt;
}
