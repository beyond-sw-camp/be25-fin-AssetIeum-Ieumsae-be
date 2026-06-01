package com.ieumsae.assetieum_be.global.response;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
	int status,
	String errorCode,
	String message,
	T data
) {

	public static <T> ApiResponse<T> ok(String message, T data) {
		return new ApiResponse<>(HttpStatus.OK.value(), null, message, data);
	}

	public static <T> ApiResponse<T> created(String message, T data) {
		return new ApiResponse<>(HttpStatus.CREATED.value(), null, message, data);
	}

	public static ApiResponse<Void> ok(String message) {
		return new ApiResponse<>(HttpStatus.OK.value(), null, message, null);
	}

	public static ApiResponse<Void> error(HttpStatus status, String errorCode, String message) {
		return new ApiResponse<>(status.value(), errorCode, message, null);
	}
}
