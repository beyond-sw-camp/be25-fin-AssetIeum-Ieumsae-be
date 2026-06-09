package com.ieumsae.assetieum.domain.member.dto;

import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberCreateResponse {

	private final UUID memberId;
	private final String memberNo;
	private final String name;
	private final String email;
	private final UUID departmentId;
	private final String departmentName;
	private final MemberRole role;
	private final MemberStatus status;
	private final LocalDateTime createdAt;

	public static MemberCreateResponse from(Member member) {
		Department department = member.getDepartment();

		return MemberCreateResponse.builder()
			.memberId(member.getId())
			.memberNo(member.getMemberNo())
			.name(member.getName())
			.email(member.getEmail())
			.departmentId(department.getId())
			.departmentName(department.getName())
			.role(member.getRole())
			.status(member.getStatus())
			.createdAt(member.getCreatedAt())
			.build();
	}
}
