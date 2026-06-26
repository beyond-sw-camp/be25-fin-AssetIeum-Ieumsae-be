package com.ieumsae.assetieum.global.config;

import com.ieumsae.assetieum.global.s3.S3Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
@ConditionalOnProperty(prefix = "cloud.aws.s3", name = "enabled", havingValue = "true")
public class S3Config {

	@Bean
	public S3Client s3Client(S3Properties properties) {
		validate(properties);
		return S3Client.builder()
			.region(Region.of(properties.getRegion()))
			.credentialsProvider(DefaultCredentialsProvider.create())
			.build();
	}

	private void validate(S3Properties properties) {
		if (!StringUtils.hasText(properties.getBucket())) {
			throw new IllegalStateException("cloud.aws.s3.bucket is required when S3 is enabled.");
		}
		if (!StringUtils.hasText(properties.getRegion())) {
			throw new IllegalStateException("cloud.aws.s3.region is required when S3 is enabled.");
		}
	}
}
