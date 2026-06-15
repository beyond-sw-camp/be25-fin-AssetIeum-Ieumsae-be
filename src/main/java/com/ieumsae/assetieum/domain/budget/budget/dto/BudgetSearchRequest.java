package com.ieumsae.assetieum.domain.budget.budget.dto;

import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BudgetSearchRequest extends PaginationRequest {

    private Integer budgetYear;

}
