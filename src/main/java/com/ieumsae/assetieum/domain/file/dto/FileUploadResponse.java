package com.ieumsae.assetieum.domain.file.dto;

import com.ieumsae.assetieum.domain.file.entity.UploadedFile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {

	private final Long fileId;
	private final String fileUrl;
	private final String originalFilename;
	private final Long fileSize;

	public static FileUploadResponse from(UploadedFile file) {
		return FileUploadResponse.builder()
			.fileId(file.getId())
			.fileUrl(file.getPath())
			.originalFilename(file.getName())
			.fileSize(file.getFileSize())
			.build();
	}
}
