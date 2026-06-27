package com.ieumsae.assetieum.domain.file.controller;

import com.ieumsae.assetieum.domain.file.dto.FileDownloadUrlResponse;
import com.ieumsae.assetieum.domain.file.dto.FileResponse;
import com.ieumsae.assetieum.domain.file.dto.FileUploadResponse;
import com.ieumsae.assetieum.domain.file.service.FileService;
import com.ieumsae.assetieum.domain.file.type.FileTargetType;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
		@RequestParam String targetId
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

	@GetMapping
	public ApiResponse<List<FileResponse>> getFiles(
		@AuthenticationPrincipal AuthenticatedMember member,
		@RequestParam FileTargetType targetType,
		@RequestParam String targetId
	) {
		List<FileResponse> response = fileService.getFiles(targetType, targetId, member.companyId());
		return ApiResponse.ok("파일 목록 조회에 성공했습니다.", response);
	}

	@GetMapping("/{fileId}/download-url")
	public ApiResponse<FileDownloadUrlResponse> createDownloadUrl(
		@AuthenticationPrincipal AuthenticatedMember member,
		@PathVariable Long fileId
	) {
		FileDownloadUrlResponse response = fileService.createDownloadUrl(fileId, member.companyId(), member.id());
		return ApiResponse.ok("파일 다운로드 URL 생성에 성공했습니다.", response);
	}

	@DeleteMapping("/{fileId}")
	public ApiResponse<Void> deleteFile(
		@AuthenticationPrincipal AuthenticatedMember member,
		@PathVariable Long fileId
	) {
		fileService.deleteFile(fileId, member.companyId());
		return ApiResponse.ok("파일 삭제에 성공했습니다.", null);
	}
}
