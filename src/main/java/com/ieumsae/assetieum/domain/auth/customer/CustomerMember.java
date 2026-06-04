package com.ieumsae.assetieum.domain.auth.customer;

import com.ieumsae.assetieum.domain.member.entity.Member;

public record CustomerMember(
	Member member,
	boolean legacyPlainPassword
) {
}
