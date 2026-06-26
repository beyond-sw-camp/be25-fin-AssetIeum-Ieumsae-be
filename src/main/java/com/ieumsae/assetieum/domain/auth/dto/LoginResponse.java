package com.ieumsae.assetieum.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"memberId",
	"memberNo",
	"name",
	"email",
	"departmentId",
	"departmentName",
	"role",
	"status",
	"accessToken",
	"tokenType",
	"expiresIn"
})
public class LoginResponse {

	private final UUID memberId;
	private final String memberNo;
	private final String name;
	private final String email;
	private final UUID departmentId;
	private final String departmentName;
	private final MemberRole role;
	private final MemberStatus status;
	private final String accessToken;
	private final String tokenType;
	private final long expiresIn;
}
