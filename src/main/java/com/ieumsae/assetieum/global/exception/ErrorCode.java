package com.ieumsae.assetieum.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

	// 회사
	COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "company-001", "회사를 찾을 수 없습니다."),


	// 유형 자산
	TANGIBLE_ASSET_CATEGORY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "tangible-asset-001", "이미 존재하는 카테고리명입니다."),
	TANGIBLE_ASSET_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "tangible-asset-002", "해당 유형 자산 카테고리는 존재하지 않습니다."),
	INVALID_PARENT_CATEGORY(HttpStatus.BAD_REQUEST, "tangible-asset-003", "유효하지 않은 부모 카테고리입니다."),

	// 공통
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

}
