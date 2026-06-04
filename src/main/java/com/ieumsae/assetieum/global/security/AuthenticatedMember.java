package com.ieumsae.assetieum.global.security;

import com.ieumsae.assetieum.domain.member.type.MemberRole;
import java.util.UUID;

public record AuthenticatedMember(
	UUID id,
	String employeeNumber,
	MemberRole role
) {
}
