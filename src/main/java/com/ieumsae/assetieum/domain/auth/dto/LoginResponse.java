package com.ieumsae.assetieum.domain.auth.dto;

import com.ieumsae.assetieum.domain.member.type.MemberRole;
import java.util.UUID;

public record LoginResponse(
	UUID memberId,
	String memberNo,
	String name,
	String email,
	UUID departmentId,
	String departmentName,
	MemberRole role,
	String accessToken
) {
}
