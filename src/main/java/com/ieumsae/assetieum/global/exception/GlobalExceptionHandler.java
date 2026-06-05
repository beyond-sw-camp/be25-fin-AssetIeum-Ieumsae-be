package com.ieumsae.assetieum.global.exception;

import com.ieumsae.assetieum.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.error(errorCode.getStatus(), errorCode.getCode(), exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception
	) {
		String message = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.findFirst()
			.map(fieldError -> fieldError.getDefaultMessage())
			.orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage());

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.error(
				HttpStatus.BAD_REQUEST,
				ErrorCode.INVALID_INPUT_VALUE.getCode(),
				message
			));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponse.error(
				HttpStatus.INTERNAL_SERVER_ERROR,
				ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
				ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
			));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
			MethodArgumentTypeMismatchException e
	) {
		return ResponseEntity
				.badRequest()
				.body(ApiResponse.error(
						HttpStatus.BAD_REQUEST,
						ErrorCode.INVALID_INPUT_VALUE.getCode(),
						ErrorCode.INVALID_INPUT_VALUE.getMessage()
				));
	}
}
