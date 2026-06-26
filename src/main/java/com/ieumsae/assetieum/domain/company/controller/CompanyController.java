package com.ieumsae.assetieum.domain.company.controller;

import com.ieumsae.assetieum.domain.company.dto.CompanyCreateRequest;
import com.ieumsae.assetieum.domain.company.dto.CompanyCreateResponse;
import com.ieumsae.assetieum.domain.company.dto.CompanyDeleteResponse;
import com.ieumsae.assetieum.domain.company.dto.CompanySearchRequest;
import com.ieumsae.assetieum.domain.company.dto.CompanySearchResponse;
import com.ieumsae.assetieum.domain.company.service.CompanyService;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies")
public class CompanyController {

	private final CompanyService companyService;

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@PostMapping
	public ApiResponse<CompanyCreateResponse> createCompany(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @RequestBody CompanyCreateRequest request
	) {
		CompanyCreateResponse response = companyService.createCompany(authenticatedMember, request);
		return ApiResponse.ok("회사 등록에 성공했습니다.", response);
	}

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@DeleteMapping("/{companyId}")
	public ApiResponse<CompanyDeleteResponse> deleteCompany(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID companyId
	) {
		CompanyDeleteResponse response = companyService.deleteCompany(authenticatedMember, companyId);
		return ApiResponse.ok("회사 삭제에 성공했습니다.", response);
	}

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@GetMapping
	public ApiResponse<PaginationResponse<CompanySearchResponse>> getCompanies(
			@Valid @ModelAttribute CompanySearchRequest request
	) {
		PaginationResponse<CompanySearchResponse> response = companyService.getCompanies(request);

		return ApiResponse.ok("회사 목록 조회에 성공했습니다.", response);
	}
}
