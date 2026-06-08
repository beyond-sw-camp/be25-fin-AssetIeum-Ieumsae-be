package com.ieumsae.assetieum.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtAuthenticationFilter jwtAuthenticationFilter,
		ObjectMapper objectMapper
	) throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) ->
					writeError(response, objectMapper, ErrorCode.INVALID_TOKEN, "인증이 필요합니다."))
				.accessDeniedHandler((request, response, accessDeniedException) ->
					writeError(response, objectMapper, ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getMessage()))
			)
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/companies").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/v1/companies/**").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.GET, "/api/v1/departments").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.GET, "/api/v1/departments/**").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/v1/departments").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.PATCH, "/api/v1/departments/**").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/v1/departments/**").hasRole("SUPER_ADMIN")
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private static void writeError(
		HttpServletResponse response,
		ObjectMapper objectMapper,
		ErrorCode errorCode,
		String message
	) throws IOException {
		response.setStatus(errorCode.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(
			response.getWriter(),
			ApiResponse.error(errorCode.getStatus(), errorCode.getCode(), message)
		);
	}
}
