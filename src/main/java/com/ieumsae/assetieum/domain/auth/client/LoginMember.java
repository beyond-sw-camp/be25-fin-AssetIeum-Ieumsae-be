package com.ieumsae.assetieum.domain.auth.client;

import com.ieumsae.assetieum.domain.member.entity.Member;
import lombok.Getter;

@Getter
public class LoginMember {

	private final Member member;
	private final boolean legacyPlainPassword;

	public LoginMember(Member member, boolean legacyPlainPassword) {
		this.member = member;
		this.legacyPlainPassword = legacyPlainPassword;
	}
}
