package com.ieumsae.assetieum.domain.auth.customer;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalCustomerMemberClient implements CustomerMemberClient {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public Optional<CustomerMember> authenticate(String employeeNumber, String rawPassword) {
		return memberRepository.findByEmployeeNumber(employeeNumber)
			.filter(member -> matches(member, rawPassword))
			.map(member -> new CustomerMember(member, isLegacyPlainPassword(member, rawPassword)));
	}

	private boolean matches(Member member, String rawPassword) {
		return passwordEncoder.matches(rawPassword, member.getPassword())
			|| isLegacyPlainPassword(member, rawPassword);
	}

	private boolean isLegacyPlainPassword(Member member, String rawPassword) {
		return rawPassword.equals(member.getPassword());
	}
}
