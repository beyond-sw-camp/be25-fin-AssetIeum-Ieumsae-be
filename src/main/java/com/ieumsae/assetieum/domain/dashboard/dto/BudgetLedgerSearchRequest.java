package com.ieumsae.assetieum.domain.dashboard.dto;

import com.ieumsae.assetieum.domain.budget.history.type.BudgetHistoryType;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BudgetLedgerSearchRequest extends PaginationRequest {

	private UUID departmentId;
	private Integer budgetYear;
	private BudgetHistoryType historyType;
	private String keyword;
}
