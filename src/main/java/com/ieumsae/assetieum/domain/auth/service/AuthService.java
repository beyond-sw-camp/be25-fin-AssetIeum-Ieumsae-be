package com.ieumsae.assetieum.domain.auth.service;

import com.ieumsae.assetieum.domain.auth.customer.CustomerMember;
import com.ieumsae.assetieum.domain.auth.customer.CustomerMemberClient;
import com.ieumsae.assetieum.domain.auth.dto.ChangePasswordRequest;
import com.ieumsae.assetieum.domain.auth.dto.LoginRequest;
import com.ieumsae.assetieum.domain.auth.dto.LoginResponse;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import com.ieumsae.assetieum.global.security.JwtProvider;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final CustomerMemberClient customerMemberClient;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;

	public AuthService(
		CustomerMemberClient customerMemberClient,
		MemberRepository memberRepository,
		PasswordEncoder passwordEncoder,
		JwtProvider jwtProvider
	) {
		this.customerMemberClient = customerMemberClient;
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtProvider = jwtProvider;
	}

	@Transactional
	public LoginResponse login(LoginRequest request) {
		CustomerMember customerMember = customerMemberClient.authenticate(request.employeeNumber(), request.password())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		Member member = customerMember.member();
		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		if (customerMember.legacyPlainPassword()) {
			member.changePassword(passwordEncoder.encode(request.password()));
		}

		return new LoginResponse(
			member.getId(),
			member.getEmployeeNumber(),
			member.getName(),
			member.getEmail(),
			member.getDepartment().getId(),
			member.getDepartment().getName(),
			member.getRole(),
			jwtProvider.createAccessToken(member)
		);
	}

	@Transactional
	public void changePassword(AuthenticatedMember authenticatedMember, ChangePasswordRequest request) {
		Member member = memberRepository.findById(authenticatedMember.id())
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}

		member.changePassword(passwordEncoder.encode(request.newPassword()));
	}
}
