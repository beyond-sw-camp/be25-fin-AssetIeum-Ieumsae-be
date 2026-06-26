package com.ieumsae.assetieum.domain.budget.history.dto;

import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BudgetHistorySearchRequest extends PaginationRequest {

    private UUID departmentId;

    private Integer budgetYear;

}
