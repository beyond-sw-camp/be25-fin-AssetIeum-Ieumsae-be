package com.ieumsae.assetieum.domain.file.controller;

import com.ieumsae.assetieum.domain.file.dto.FileUploadResponse;
import com.ieumsae.assetieum.domain.file.service.FileService;
import com.ieumsae.assetieum.domain.file.type.FileTargetType;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileController {

	private final FileService fileService;

	@PostMapping
	public ApiResponse<FileUploadResponse> upload(
		@AuthenticationPrincipal AuthenticatedMember member,
		@RequestPart("file") MultipartFile file,
		@RequestParam FileTargetType targetType,
		@RequestParam UUID targetId
	) {
		FileUploadResponse response = fileService.upload(
			file,
			targetType,
			targetId,
			member.companyId(),
			member.id()
		);

		return ApiResponse.ok("파일 업로드에 성공했습니다.", response);
	}
}
