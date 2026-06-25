package com.ieumsae.assetieum.global.health;

import com.ieumsae.assetieum.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

	@GetMapping("/api/v1/health")
	public ApiResponse<HealthCheckResponse> health() {
		return ApiResponse.ok("헬스체크에 성공했습니다.", new HealthCheckResponse("UP"));
	}

	public record HealthCheckResponse(String status) {
	}
}
