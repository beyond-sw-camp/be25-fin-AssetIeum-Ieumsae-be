package com.ieumsae.assetieum.domain.company.controller;

import com.ieumsae.assetieum.domain.company.dto.CompanyResponse;
import com.ieumsae.assetieum.domain.company.dto.CreateCompanyRequest;
import com.ieumsae.assetieum.domain.company.service.CompanyService;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompanyController {

	private final CompanyService companyService;

	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}

	@PostMapping("/api/v1/companies")
	public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody CreateCompanyRequest request
	) {
		CompanyResponse response = companyService.createCompany(authenticatedMember, request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.created("회사가 등록되었습니다.", response));
	}
}
