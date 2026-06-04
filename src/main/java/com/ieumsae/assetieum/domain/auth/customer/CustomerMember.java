package com.ieumsae.assetieum.domain.auth.customer;

import com.ieumsae.assetieum.domain.member.entity.Member;
import lombok.Getter;

@Getter
public class CustomerMember {

	private final Member member;
	private final boolean legacyPlainPassword;

	public CustomerMember(Member member, boolean legacyPlainPassword) {
		this.member = member;
		this.legacyPlainPassword = legacyPlainPassword;
	}
}
