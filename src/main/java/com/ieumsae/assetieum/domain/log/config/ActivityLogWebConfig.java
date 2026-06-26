package com.ieumsae.assetieum.domain.log.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ActivityLogWebConfig implements WebMvcConfigurer {

	private final ActivityLogInterceptor activityLogInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(activityLogInterceptor)
			.addPathPatterns("/api/v1/**");
	}
}
