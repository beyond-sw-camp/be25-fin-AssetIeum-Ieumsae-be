package com.ieumsae.assetieum.domain.company.repository;

import com.ieumsae.assetieum.domain.company.dto.CompanySearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyRepositoryCustom {

    Page<CompanySearchResponse> search(String keyword, Pageable pageable);
}
