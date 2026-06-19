package com.ieumsae.assetieum.domain.budget.history.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.budget.history.type.BudgetHistoryType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "historyId",
        "departmentId",
        "departmentName",
        "budgetId",
        "budgetYear",
        "ticketId",
        "ticketNo",
        "purchasePlanId",
        "purchasePlanNo",
        "historyType",
        "amount",
        "usedAmountBefore",
        "usedAmountAfter",
        "holdAmountBefore",
        "holdAmountAfter",
        "totalBudget",
        "description",
        "createdAt"
})
public class BudgetHistoryResponse {

    private Long historyId;

    private UUID departmentId;

    private String departmentName;

    private UUID budgetId;

    private Integer budgetYear;

    private UUID ticketId;

    private String ticketNo;

    private UUID purchasePlanId;

    private String purchasePlanNo;

    private BudgetHistoryType historyType;

    private BigDecimal amount;

    private BigDecimal usedAmountBefore;

    private BigDecimal usedAmountAfter;

    private BigDecimal holdAmountBefore;

    private BigDecimal holdAmountAfter;

    private BigDecimal totalBudget;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

}
