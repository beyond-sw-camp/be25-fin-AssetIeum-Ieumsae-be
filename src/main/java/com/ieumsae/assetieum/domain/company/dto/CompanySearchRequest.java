package com.ieumsae.assetieum.domain.company.dto;

import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanySearchRequest extends PaginationRequest {

    private String keyword;

}
