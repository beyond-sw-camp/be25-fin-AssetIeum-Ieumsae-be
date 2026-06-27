package com.ieumsae.assetieum.global.s3;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(S3Client.class)
public class S3Uploader {

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
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

	public void delete(String key) {
		if (!StringUtils.hasText(key)) {
			return;
		}
		s3Client.deleteObject(DeleteObjectRequest.builder()
			.bucket(properties.getBucket())
			.key(key)
			.build());
	}

	public void deleteByUrl(String url) {
		String key = resolveKeyFromUrl(url);
		delete(key);
	}

	public String createPresignedGetUrl(String url, Duration duration) {
		String key = resolveKeyFromUrl(url);
		if (!StringUtils.hasText(key)) {
			throw new IllegalArgumentException("Invalid S3 object URL.");
		}

		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(properties.getBucket())
			.key(key)
			.build();
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(duration)
			.getObjectRequest(getObjectRequest)
			.build();

		return s3Presigner.presignGetObject(presignRequest).url().toString();
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
		}
		if (file.getSize() > properties.getMaxFileSizeBytes()) {
			throw new IllegalArgumentException("업로드 가능한 파일 크기를 초과했습니다.");
		}
		String extension = resolveExtension(file.getOriginalFilename()).replace(".", "");
		if (!StringUtils.hasText(extension) || !allowedExtensions().contains(extension)) {
			throw new IllegalArgumentException("허용되지 않은 파일 확장자입니다.");
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

	private String resolveKeyFromUrl(String url) {
		if (!StringUtils.hasText(url)) {
			return null;
		}
		String normalizedBaseUrl = StringUtils.hasText(properties.getPublicBaseUrl())
			? properties.getPublicBaseUrl().replaceAll("/+$", "")
			: "https://" + properties.getBucket() + ".s3." + properties.getRegion() + ".amazonaws.com";
		if (url.startsWith(normalizedBaseUrl + "/")) {
			return decodeKey(url.substring(normalizedBaseUrl.length() + 1));
		}

		URI uri = URI.create(url);
		String path = uri.getPath();
		if (!StringUtils.hasText(path)) {
			return null;
		}
		return decodeKey(path.replaceFirst("^/+", ""));
	}

	private String decodeKey(String key) {
		return URLDecoder.decode(key, StandardCharsets.UTF_8);
	}

	private Set<String> allowedExtensions() {
		return Arrays.stream(properties.getAllowedExtensions().split(","))
			.map(value -> value.trim().toLowerCase(Locale.ROOT))
			.filter(StringUtils::hasText)
			.collect(Collectors.toSet());
	}
}
