package com.ieumsae.assetieum.domain.member.dto;

import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDepartmentUpdateResponse {

	private final UUID memberId;
	private final String memberNo;
	private final String name;
	private final UUID previousDepartmentId;
	private final String previousDepartmentName;
	private final UUID currentDepartmentId;
	private final String currentDepartmentName;
	private final LocalDateTime updatedAt;

	public static MemberDepartmentUpdateResponse from(Member member, Department previousDepartment) {
		Department currentDepartment = member.getDepartment();

		return MemberDepartmentUpdateResponse.builder()
			.memberId(member.getId())
			.memberNo(member.getMemberNo())
			.name(member.getName())
			.previousDepartmentId(previousDepartment.getId())
			.previousDepartmentName(previousDepartment.getName())
			.currentDepartmentId(currentDepartment.getId())
			.currentDepartmentName(currentDepartment.getName())
			.updatedAt(member.getUpdatedAt())
			.build();
	}
}
