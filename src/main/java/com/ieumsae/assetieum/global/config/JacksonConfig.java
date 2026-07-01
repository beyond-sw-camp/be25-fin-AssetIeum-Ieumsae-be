package com.ieumsae.assetieum.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

	private static final String DEFAULT_TIME_ZONE = "Asia/Seoul";

	@Bean
	public ObjectMapper objectMapper() {
		return JsonMapper.builder()
			.findAndAddModules()
			.defaultTimeZone(TimeZone.getTimeZone(DEFAULT_TIME_ZONE))
			.build();
	}
}
