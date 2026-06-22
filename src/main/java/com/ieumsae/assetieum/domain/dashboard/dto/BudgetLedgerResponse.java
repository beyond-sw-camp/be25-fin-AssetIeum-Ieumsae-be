package com.ieumsae.assetieum.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.budget.history.type.BudgetHistoryType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"historyId",
	"date",
	"departmentId",
	"departmentName",
	"type",
	"historyType",
	"usage",
	"amount",
	"balance",
	"ticketId",
	"ticketNo",
	"purchasePlanId",
	"purchasePlanNo"
})
public class BudgetLedgerResponse {

	private final Long historyId;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime date;

	private final UUID departmentId;
	private final String departmentName;
	private final String type;
	private final BudgetHistoryType historyType;
	private final String usage;
	private final BigDecimal amount;
	private final BigDecimal balance;
	private final UUID ticketId;
	private final String ticketNo;
	private final UUID purchasePlanId;
	private final String purchasePlanNo;
}
