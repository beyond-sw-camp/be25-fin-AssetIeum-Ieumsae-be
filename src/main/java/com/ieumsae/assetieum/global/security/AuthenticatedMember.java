package com.ieumsae.assetieum.global.security;

import com.ieumsae.assetieum.domain.member.type.MemberRole;
import java.util.UUID;

public record AuthenticatedMember(
	UUID id,
	UUID companyId,
	String memberNo,
	String name,
	String email,
	MemberRole role
) {
}
