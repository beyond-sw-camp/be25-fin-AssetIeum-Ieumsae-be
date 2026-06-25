package com.ieumsae.assetieum.global.s3;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(S3Client.class)
public class S3Uploader {

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	private final S3Client s3Client;
	private final S3Properties properties;

	public S3UploadResult upload(MultipartFile file, String directory) {
		validateFile(file);

		String key = createObjectKey(file.getOriginalFilename(), directory);
		String contentType = resolveContentType(file);

		PutObjectRequest request = PutObjectRequest.builder()
			.bucket(properties.getBucket())
			.key(key)
			.contentType(contentType)
			.contentLength(file.getSize())
			.build();

		try {
			s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to read upload file.", e);
		}

		return new S3UploadResult(
			key,
			createObjectUrl(key),
			file.getOriginalFilename(),
			contentType,
			file.getSize()
		);
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Upload file must not be empty.");
		}
	}

	private String createObjectKey(String originalFilename, String directory) {
		String extension = resolveExtension(originalFilename);
		String prefix = normalizeDirectory(directory);
		return prefix + UUID.randomUUID() + extension;
	}

	private String normalizeDirectory(String directory) {
		if (!StringUtils.hasText(directory)) {
			return "";
		}
		String normalized = directory.trim()
			.replace("\\", "/")
			.replaceAll("^/+", "")
			.replaceAll("/+$", "");
		return normalized.isBlank() ? "" : normalized + "/";
	}

	private String resolveExtension(String originalFilename) {
		if (!StringUtils.hasText(originalFilename)) {
			return "";
		}
		String filename = originalFilename.replace("\\", "/");
		filename = filename.substring(filename.lastIndexOf('/') + 1);
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == filename.length() - 1) {
			return "";
		}
		return filename.substring(dotIndex).toLowerCase();
	}

	private String resolveContentType(MultipartFile file) {
		return StringUtils.hasText(file.getContentType()) ? file.getContentType() : DEFAULT_CONTENT_TYPE;
	}

	private String createObjectUrl(String key) {
		if (StringUtils.hasText(properties.getPublicBaseUrl())) {
			return properties.getPublicBaseUrl().replaceAll("/+$", "") + "/" + encodeKey(key);
		}
		return "https://" + properties.getBucket() + ".s3." + properties.getRegion() + ".amazonaws.com/" + encodeKey(key);
	}

	private String encodeKey(String key) {
		return URLEncoder.encode(key, StandardCharsets.UTF_8).replace("+", "%20").replace("%2F", "/");
	}
}
