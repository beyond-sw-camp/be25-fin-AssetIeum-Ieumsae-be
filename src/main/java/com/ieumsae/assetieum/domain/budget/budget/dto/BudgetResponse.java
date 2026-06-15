package com.ieumsae.assetieum.domain.budget.budget.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.budget.budget.entity.Budget;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
        "budgetId",
        "budgetYear",
        "departmentId",
        "departmentName",
        "totalAmount",
        "heldAmount",
        "usedAmount",
        "createdAt",
        "updatedAt"
})
public class BudgetResponse {

    private UUID budgetId;

    private Integer budgetYear;

    private UUID departmentId;

    private String departmentName;

    private BigDecimal totalAmount;

    private BigDecimal heldAmount;

    private BigDecimal usedAmount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static BudgetResponse from(Budget budget) {
        return BudgetResponse.builder()
                .budgetId(budget.getId())
                .budgetYear(budget.getBudgetYear())
                .departmentId(
                        budget.getDepartment() != null
                                ? budget.getDepartment().getId()
                                : null
                )
                .departmentName(
                        budget.getDepartment() != null
                                ? budget.getDepartment().getName()
                                : null
                )
                .totalAmount(budget.getTotalAmount())
                .heldAmount(budget.getHeldAmount())
                .usedAmount(budget.getUsedAmount())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
