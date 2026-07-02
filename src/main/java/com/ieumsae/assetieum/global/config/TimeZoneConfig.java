package com.ieumsae.assetieum.global.config;

import com.ieumsae.assetieum.global.common.util.KstDateTime;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;

@Configuration
public class TimeZoneConfig {

	private static final String DEFAULT_TIME_ZONE = "Asia/Seoul";

	@PostConstruct
	public void setDefaultTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIME_ZONE));
	}

	@Bean
	public DateTimeProvider seoulDateTimeProvider() {
		return () -> Optional.of(KstDateTime.now());
	}
}
