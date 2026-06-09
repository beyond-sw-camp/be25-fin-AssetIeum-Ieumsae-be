package com.ieumsae.assetieum.domain.member.dto;

import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberListItemResponse {

	private final UUID memberId;
	private final String memberNo;
	private final String name;
	private final String email;
	private final String departmentNamePath;
	private final MemberRole role;
	private final MemberStatus status;
	private final LocalDate joinDate;

	public static MemberListItemResponse from(Member member) {
		return MemberListItemResponse.builder()
			.memberId(member.getId())
			.memberNo(member.getMemberNo())
			.name(member.getName())
			.email(member.getEmail())
			.departmentNamePath(buildDepartmentNamePath(member.getDepartment()))
			.role(member.getRole())
			.status(member.getStatus())
			.joinDate(member.getCreatedAt().toLocalDate())
			.build();
	}

	private static String buildDepartmentNamePath(Department department) {
		Deque<String> names = new ArrayDeque<>();
		Department current = department;

		while (current != null) {
			names.addFirst(current.getName());
			current = current.getParentDepartment();
		}

		return String.join(" > ", names);
	}
}
