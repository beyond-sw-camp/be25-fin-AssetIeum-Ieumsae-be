package com.ieumsae.assetieum;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class AssetieumBeApplication {

	public static void main(String[] args) {
		loadRequiredEnv();
		SpringApplication.run(AssetieumBeApplication.class, args);
	}

	private static void loadRequiredEnv() {
		Path envPath = findEnvPath()
				.orElseThrow(() -> new IllegalStateException(".env file is required in the project root."));
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

		try {
			List<String> lines = Files.readAllLines(envPath);
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

				System.setProperty(key, value);
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read .env file.", e);
		}

		for (String key : requiredKeys) {
			if (System.getProperty(key) == null || System.getProperty(key).isBlank()) {
				throw new IllegalStateException("Required .env key is missing: " + key);
			}
		}
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
