package com.ieumsae.assetieum.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"newPurchaseQuantity",
	"newPurchaseQuantityChangeRate",
	"departmentPurchaseRequests"
})
public class PurchaseOperationReportResponse {

	private final long newPurchaseQuantity;
	private final BigDecimal newPurchaseQuantityChangeRate;
	private final PaginationResponse<DepartmentPurchaseRequestSummary> departmentPurchaseRequests;

	@Getter
	@Builder
	@JsonPropertyOrder({
		"departmentId",
		"departmentName",
		"purchaseRequestCount",
		"purchaseApprovedCount",
		"purchaseCompletedCount",
		"accumulatedPurchaseQuantity"
	})
	public static class DepartmentPurchaseRequestSummary {
		private final UUID departmentId;
		private final String departmentName;
		private final long purchaseRequestCount;
		private final long purchaseApprovedCount;
		private final long purchaseCompletedCount;
		private final long accumulatedPurchaseQuantity;
	}
}
