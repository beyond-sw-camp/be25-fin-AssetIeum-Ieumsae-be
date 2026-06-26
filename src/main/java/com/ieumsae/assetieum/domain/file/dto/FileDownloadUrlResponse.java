package com.ieumsae.assetieum.domain.file.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDownloadUrlResponse {

	private final String downloadUrl;
	private final long expiresInSeconds;

	public static FileDownloadUrlResponse of(String downloadUrl, long expiresInSeconds) {
		return FileDownloadUrlResponse.builder()
			.downloadUrl(downloadUrl)
			.expiresInSeconds(expiresInSeconds)
			.build();
	}
}
