package com.ieumsae.assetieum.global.s3;

public record S3UploadResult(
	String key,
	String url,
	String originalFilename,
	String contentType,
	long size
) {
}
