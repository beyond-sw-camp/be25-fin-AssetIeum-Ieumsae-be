package com.ieumsae.assetieum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

@EnableJpaAuditing(dateTimeProviderRef = "seoulDateTimeProvider")
@EnableScheduling
@SpringBootApplication
public class AssetieumBeApplication {

	private static final String DEFAULT_TIME_ZONE = "Asia/Seoul";

	public static void main(String[] args) {
		setDefaultTimeZone();
		loadEnvIfExists();
		validateRequiredEnv();
		SpringApplication.run(AssetieumBeApplication.class, args);
	}

	private static void setDefaultTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIME_ZONE));
		System.setProperty("user.timezone", DEFAULT_TIME_ZONE);
	}

	private static void loadEnvIfExists() {
		Optional<Path> envPath = findEnvPath();
		if (envPath.isEmpty()) {
			return;
		}

		try {
			List<String> lines = Files.readAllLines(envPath.get());
			for (String line : lines) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#")) {
					continue;
				}

				int separatorIndex = trimmed.indexOf('=');
				if (separatorIndex <= 0) {
					continue;
				}

				String key = trimmed.substring(0, separatorIndex).trim();
				String value = trimmed.substring(separatorIndex + 1).trim();
				if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
					value = value.substring(1, value.length() - 1);
				}

				// Docker/ECS 등에서 주입한 환경변수가 있으면 .env 값으로 덮어쓰지 않는다.
				if (!hasConfiguredValue(key)) {
					System.setProperty(key, value);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read .env file.", e);
		}
	}

	private static void validateRequiredEnv() {
		Set<String> requiredKeys = Set.of(
				"DB_URL",
				"DB_USERNAME",
				"DB_PASSWORD",
				"JWT_SECRET",
				"JWT_ISSUER",
				"JWT_AUDIENCE",
				"JWT_ACCESS_TOKEN_EXPIRATION_MINUTES",
				"JWT_REFRESH_TOKEN_EXPIRATION_DAYS"
		);

		for (String key : requiredKeys) {
			if (!hasConfiguredValue(key)) {
				throw new IllegalStateException("Required environment variable is missing: " + key);
			}
		}
	}

	private static boolean hasConfiguredValue(String key) {
		String propertyValue = System.getProperty(key);
		if (propertyValue != null && !propertyValue.isBlank()) {
			return true;
		}
		String environmentValue = System.getenv(key);
		return environmentValue != null && !environmentValue.isBlank();
	}

	private static Optional<Path> findEnvPath() {
		Optional<Path> envPath = findEnvInParents(Path.of("").toAbsolutePath());
		if (envPath.isPresent()) {
			return envPath;
		}

		String classPath = System.getProperty("java.class.path", "");
		for (String entry : classPath.split(File.pathSeparator)) {
			envPath = findEnvInParents(Path.of(entry).toAbsolutePath());
			if (envPath.isPresent()) {
				return envPath;
			}
		}

		return Optional.empty();
	}

	private static Optional<Path> findEnvInParents(Path startPath) {
		Path current = Files.isDirectory(startPath) ? startPath : startPath.getParent();
		while (current != null) {
			Path envPath = current.resolve(".env");
			if (Files.exists(envPath)) {
				return Optional.of(envPath);
			}

			current = current.getParent();
		}
		return Optional.empty();
	}

}
