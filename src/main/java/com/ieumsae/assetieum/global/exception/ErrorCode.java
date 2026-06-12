package com.ieumsae.assetieum.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
	// 인증
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_001", "사번 또는 비밀번호가 올바르지 않습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 인증 토큰입니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_003", "접근 권한이 없습니다."),
	REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "AUTH_004", "리프레시 토큰 재사용이 감지되었습니다. 다시 로그인해 주세요."),

	// 멤버
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "멤버를 찾을 수 없습니다."),
	INACTIVE_MEMBER(HttpStatus.FORBIDDEN, "MEMBER_002", "활성 상태가 아닌 멤버입니다."),
	MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_003", "이미 등록된 사번입니다."),
	MEMBER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_004", "이미 등록된 이메일입니다."),

	// 회사
	COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "company-001", "회사를 찾을 수 없습니다."),
	COMPANY_ALREADY_EXISTS(HttpStatus.CONFLICT, "company-002", "이미 등록된 회사 코드입니다."),
	ACCESS_DENIED_COMPANY_SCOPE(HttpStatus.BAD_REQUEST, "company-003", "동일 회사의 데이터만 접근할 수 있습니다."),

	// 부서
	DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "department-001", "부서를 찾을 수 없습니다."),
	INVALID_DEPARTMENT_MANAGER(HttpStatus.BAD_REQUEST, "department-002", "유효하지 않은 부서장입니다."),
	DEPARTMENT_HAS_CHILDREN(HttpStatus.CONFLICT, "department-003", "하위 부서가 존재하는 부서는 삭제할 수 없습니다."),
	DEPARTMENT_HAS_MEMBERS(HttpStatus.CONFLICT, "department-004", "소속 사원이 존재하는 부서는 삭제할 수 없습니다."),
	INVALID_PARENT_DEPARTMENT(HttpStatus.BAD_REQUEST, "department-005", "유효하지 않은 상위 부서입니다."),

	// 유형 자산 카테고리
	TANGIBLE_ASSET_CATEGORY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "tangible-asset-001", "이미 존재하는 카테고리명입니다."),
	TANGIBLE_ASSET_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "tangible-asset-002", "해당 유형 자산 카테고리가 존재하지 않습니다."),
	TANGIBLE_ASSET_INVALID_PARENT(HttpStatus.BAD_REQUEST, "tangible-asset-003", "유효하지 않은 부모 카테고리입니다."),
	TANGIBLE_ASSET_CATEGORY_HAS_CHILDREN(HttpStatus.CONFLICT, "tangible-asset-004", "하위 카테고리가 존재하는 경우 삭제할 수 없습니다."),
	TANGIBLE_ASSET_CATEGORY_HAS_ITEMS(HttpStatus.CONFLICT, "tangible-asset-005", "품목이 존재하는 경우 삭제할 수 없습니다."),

	// 유형 자산 품목
	TANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "tangible-asset-006", "이미 존재하는 제품명입니다."),
	TANGIBLE_ASSET_ITEM_DUPLICATED_MODEL_NAME(HttpStatus.BAD_REQUEST, "tangible-asset-007", "이미 존재하는 모델명입니다."),
	TANGIBLE_ASSET_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "tangible-asset-008", "해당 유형 자산 품목은 존재하지 않습니다."),
	TANGIBLE_ASSET_ITEM_HAS_ASSETS(HttpStatus.CONFLICT, "tangible-asset-009", "자산이 존재하는 경우 삭제할 수 없습니다."),

	// 유형 자산
	TANGIBLE_ASSET_ITEM_DUPLICATED_SERIAL_NUMBER(HttpStatus.BAD_REQUEST, "tangible-asset-010", "이미 존재하는 시리얼넘버입니다."),
	TANGIBLE_ASSET_NOT_FOUND(HttpStatus.NOT_FOUND, "tangible-asset-011", "해당 유형 자산이 존재하지 않습니다."),
	TANGIBLE_ASSET_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "tangible-asset-012", "잘못된 자산 요청 값입니다."),

	// 무형 자산 카테고리
	INTANGIBLE_ASSET_CATEGORY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "intangible-asset-001", "이미 존재하는 카테고리명입니다."),
	INTANGIBLE_ASSET_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "intangible-asset-002", "해당 무형 자산 카테고리가 존재하지 않습니다."),
	INTANGIBLE_ASSET_INVALID_PARENT(HttpStatus.BAD_REQUEST, "intangible-asset-003", "유효하지 않은 부모 카테고리입니다."),
	INTANGIBLE_ASSET_CATEGORY_HAS_CHILDREN(HttpStatus.CONFLICT, "intangible-asset-004", "하위 카테고리가 존재하는 경우 삭제할 수 없습니다."),
	INTANGIBLE_ASSET_CATEGORY_HAS_ITEMS(HttpStatus.CONFLICT, "intangible-asset-005", "품목이 존재하는 경우 삭제할 수 없습니다."),

	// 무형 자산 품목
	INTANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "intangible-asset-005", "이미 존재하는 제품명입니다."),
	INTANGIBLE_ASSET_ITEM_DUPLICATED_MODEL_NAME(HttpStatus.BAD_REQUEST, "intangible-asset-006", "이미 존재하는 모델명입니다."),
	INTANGIBLE_ASSET_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "intangible-asset-007", "해당 무형 자산 품목은 존재하지 않습니다."),
	INTANGIBLE_ASSET_ITEM_HAS_ASSETS(HttpStatus.CONFLICT, "intangible-asset-008", "자산이 존재하는 경우 삭제할 수 없습니다."),

	// 무형 자산
	INTANGIBLE_ASSET_ITEM_DUPLICATED_LICENSE_CODE(HttpStatus.BAD_REQUEST, "intangible-asset-009", "이미 존재하는 라이선스 코드입니다."),
	INTANGIBLE_ASSET_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "intangible-asset-010", "잘못된 자산 요청 값입니다."),

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
