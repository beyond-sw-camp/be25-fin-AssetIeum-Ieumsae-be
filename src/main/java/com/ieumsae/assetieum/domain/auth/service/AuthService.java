package com.ieumsae.assetieum.domain.auth.service;

import com.ieumsae.assetieum.domain.auth.client.LoginMember;
import com.ieumsae.assetieum.domain.auth.client.LoginMemberClient;
import com.ieumsae.assetieum.domain.auth.dto.ChangePasswordRequest;
import com.ieumsae.assetieum.domain.auth.dto.ChangePasswordResponse;
import com.ieumsae.assetieum.domain.auth.dto.LoginRequest;
import com.ieumsae.assetieum.domain.auth.dto.LoginResponse;
import com.ieumsae.assetieum.domain.auth.dto.TokenReissueResponse;
import com.ieumsae.assetieum.domain.log.service.LogService;
import com.ieumsae.assetieum.domain.log.type.ActivityLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import com.ieumsae.assetieum.global.security.JwtProvider;
import com.ieumsae.assetieum.global.security.JwtProperties;
import com.ieumsae.assetieum.global.security.TokenRedisService;
import com.ieumsae.assetieum.global.security.TokenRedisService.RefreshTokenStatus;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final LoginMemberClient loginMemberClient;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final JwtProperties jwtProperties;
	private final TokenRedisService tokenRedisService;
	private final LogService logService;

	@Transactional
	public LoginResult login(LoginRequest request) {
		LoginMember loginMember = loginMemberClient.authenticate(
				request.getCompanyCode(),
				request.getMemberNo(),
				request.getPassword()
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		Member member = loginMember.getMember();
		if (!member.canLogin()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		if (loginMember.isLegacyPlainPassword()) {
			member.changePassword(passwordEncoder.encode(request.getPassword()));
		}

		String accessToken = jwtProvider.createAccessToken(member);
		String refreshToken = jwtProvider.createRefreshToken(member);
		tokenRedisService.saveRefreshToken(member.getId(), refreshToken, jwtProvider.getRefreshTokenExpiresInSeconds());

		LoginResponse response = LoginResponse.builder()
			.memberId(member.getId())
			.memberNo(member.getMemberNo())
			.name(member.getName())
			.email(member.getEmail())
			.departmentId(member.getDepartment().getId())
			.departmentName(member.getDepartment().getName())
			.role(member.getRole())
			.status(member.getStatus())
			.accessToken(accessToken)
			.tokenType("Bearer")
			.expiresIn(jwtProvider.getAccessTokenExpiresInSeconds())
			.build();
		logService.recordActivityLog(
			member,
			ActivityLogAction.LOGIN,
			LogSubjectType.MEMBER,
			member.getId(),
			member.getName() + " logged in."
		);

		return new LoginResult(response, refreshToken);
	}

	@Transactional
	public ReissueResult reissue(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}

		AuthenticatedMember authenticatedMember = jwtProvider.parseRefreshToken(refreshToken);
		RefreshTokenStatus refreshTokenStatus = tokenRedisService.getRefreshTokenStatus(
			authenticatedMember.id(),
			refreshToken
		);
		if (refreshTokenStatus == RefreshTokenStatus.NONE) {
			tokenRedisService.deleteRefreshToken(authenticatedMember.id());
			throw new BusinessException(ErrorCode.REFRESH_TOKEN_REUSED);
		}

		Member member = memberRepository.findById(authenticatedMember.id())
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.canLogin()) {
			tokenRedisService.deleteRefreshToken(member.getId());
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		String newAccessToken = jwtProvider.createAccessToken(member);
		String newRefreshToken = null;
		if (refreshTokenStatus == RefreshTokenStatus.CURRENT) {
			newRefreshToken = jwtProvider.createRefreshToken(member);
			tokenRedisService.rotateRefreshToken(
				member.getId(),
				refreshToken,
				newRefreshToken,
				jwtProvider.getRefreshTokenExpiresInSeconds(),
				jwtProperties.getRefreshTokenGracePeriodSeconds()
			);
		}

		return new ReissueResult(
			TokenReissueResponse.from(newAccessToken, jwtProvider.getAccessTokenExpiresInSeconds()),
			newRefreshToken
		);
	}

	@Transactional
	public void logout(AuthenticatedMember authenticatedMember, String accessToken) {
		tokenRedisService.deleteRefreshToken(authenticatedMember.id());

		if (accessToken != null && !accessToken.isBlank()) {
			long remainingSeconds = jwtProvider.getRemainingSeconds(accessToken);
			tokenRedisService.blacklistAccessToken(accessToken, remainingSeconds);
		}
	}

	@Transactional
	public ChangePasswordResponse changePassword(
		AuthenticatedMember authenticatedMember,
		ChangePasswordRequest request
	) {
		Member member = memberRepository.findById(authenticatedMember.id())
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.canLogin()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}

		if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}

		if (!request.getNewPassword().equals(request.getConfirmPassword())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.");
		}

		member.changePassword(passwordEncoder.encode(request.getNewPassword()));
		return ChangePasswordResponse.builder()
			.memberId(member.getId())
			.updatedAt(LocalDateTime.now())
			.build();
	}

	public record LoginResult(LoginResponse response, String refreshToken) {
	}

	public record ReissueResult(TokenReissueResponse response, String refreshToken) {
	}
}
