package com.ieumsae.assetieum.domain.auth.dto;

import com.ieumsae.assetieum.domain.member.type.MemberRole;
import java.util.UUID;
import lombok.Getter;

@Getter
public class LoginResponse {

	private final UUID memberId;
	private final String memberNo;
	private final String name;
	private final String email;
	private final UUID departmentId;
	private final String departmentName;
	private final MemberRole role;
	private final String accessToken;

	public LoginResponse(
		UUID memberId,
		String memberNo,
		String name,
		String email,
		UUID departmentId,
		String departmentName,
		MemberRole role,
		String accessToken
	) {
		this.memberId = memberId;
		this.memberNo = memberNo;
		this.name = name;
		this.email = email;
		this.departmentId = departmentId;
		this.departmentName = departmentName;
		this.role = role;
		this.accessToken = accessToken;
	}
}
