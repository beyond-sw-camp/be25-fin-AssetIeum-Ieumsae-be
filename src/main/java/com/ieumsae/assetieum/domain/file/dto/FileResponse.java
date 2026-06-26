package com.ieumsae.assetieum.domain.file.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.file.entity.UploadedFile;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileResponse {

	private final Long fileId;
	private final String fileUrl;
	private final String originalFilename;
	private final Long fileSize;
	private final String extension;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime uploadedAt;

	public static FileResponse from(UploadedFile file) {
		return FileResponse.builder()
			.fileId(file.getId())
			.fileUrl(file.getPath())
			.originalFilename(file.getName())
			.fileSize(file.getFileSize())
			.extension(file.getExtension())
			.uploadedAt(file.getCreatedAt())
			.build();
	}
}
