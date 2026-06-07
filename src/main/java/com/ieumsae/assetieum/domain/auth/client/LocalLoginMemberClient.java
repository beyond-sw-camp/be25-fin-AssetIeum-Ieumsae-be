package com.ieumsae.assetieum.domain.auth.client;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LocalLoginMemberClient implements LoginMemberClient {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public Optional<LoginMember> authenticate(String companyCode, String memberNo, String rawPassword) {
		return memberRepository.findByMemberNoAndCompany_CompanyCode(memberNo, companyCode)
			.flatMap(member -> authenticate(member, rawPassword));
	}

	private Optional<LoginMember> authenticate(Member member, String rawPassword) {
		if (!matches(member, rawPassword)) {
			return Optional.empty();
		}

		return Optional.of(new LoginMember(member, isLegacyPlainPassword(member)));
	}

	private boolean matches(Member member, String rawPassword) {
		if (isLegacyPlainPassword(member)) {
			return rawPassword.equals(member.getPassword());
		}

		return passwordEncoder.matches(rawPassword, member.getPassword());
	}

	private boolean isLegacyPlainPassword(Member member) {
		String password = member.getPassword();
		return !password.startsWith("$2a$")
			&& !password.startsWith("$2b$")
			&& !password.startsWith("$2y$");
	}
}
