package com.ieumsae.assetieum.domain.auth.service;

import com.ieumsae.assetieum.domain.auth.customer.CustomerMember;
import com.ieumsae.assetieum.domain.auth.customer.CustomerMemberClient;
import com.ieumsae.assetieum.domain.auth.dto.ChangePasswordRequest;
import com.ieumsae.assetieum.domain.auth.dto.ChangePasswordResponse;
import com.ieumsae.assetieum.domain.auth.dto.LoginRequest;
import com.ieumsae.assetieum.domain.auth.dto.LoginResponse;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import com.ieumsae.assetieum.global.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final CustomerMemberClient customerMemberClient;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;

	@Transactional
	public LoginResponse login(LoginRequest request) {
		CustomerMember customerMember = customerMemberClient.authenticate(
				request.getEmployeeNumber(),
				request.getPassword()
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		Member member = customerMember.getMember();
		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		// 초기 비밀번호가 사번 같은 평문으로 들어온 경우, 첫 로그인 성공 시 해시 비밀번호로 전환한다.
		if (customerMember.isLegacyPlainPassword()) {
			member.changePassword(passwordEncoder.encode(request.getPassword()));
		}

		return LoginResponse.builder()
			.memberId(member.getId())
			.memberNo(member.getEmployeeNumber())
			.name(member.getName())
			.email(member.getEmail())
			.departmentId(member.getDepartment().getId())
			.departmentName(member.getDepartment().getName())
			.role(member.getRole())
			.status(member.getStatus())
			.accessToken(jwtProvider.createAccessToken(member))
			.build();
	}

	@Transactional
	public ChangePasswordResponse changePassword(
		AuthenticatedMember authenticatedMember,
		ChangePasswordRequest request
	) {
		Member member = memberRepository.findById(authenticatedMember.id())
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		// 토큰은 로그인 시점의 정보이므로, 보안 작업 전에는 DB 기준 현재 상태를 다시 확인한다.
		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}

		member.changePassword(passwordEncoder.encode(request.getNewPassword()));
		return ChangePasswordResponse.builder()
			.memberId(member.getId())
			.updatedAt(LocalDateTime.now())
			.build();
	}
}
