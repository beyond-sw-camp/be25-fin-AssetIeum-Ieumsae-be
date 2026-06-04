package com.ieumsae.assetieum.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_001", "사번 또는 비밀번호가 올바르지 않습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 인증 토큰입니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_003", "접근 권한이 없습니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "멤버를 찾을 수 없습니다."),
	INACTIVE_MEMBER(HttpStatus.FORBIDDEN, "MEMBER_002", "활성 상태가 아닌 멤버입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
