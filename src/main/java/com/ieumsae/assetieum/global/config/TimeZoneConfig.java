package com.ieumsae.assetieum.global.config;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;

@Configuration
public class TimeZoneConfig {

	private static final String DEFAULT_TIME_ZONE = "Asia/Seoul";
	private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of(DEFAULT_TIME_ZONE);

	@PostConstruct
	public void setDefaultTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIME_ZONE));
	}

	@Bean
	public DateTimeProvider seoulDateTimeProvider() {
		return () -> Optional.of(LocalDateTime.now(DEFAULT_ZONE_ID));
	}
}
