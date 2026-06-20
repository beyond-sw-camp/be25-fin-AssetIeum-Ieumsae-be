package com.ieumsae.assetieum.domain.ticket.maintenance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceTicketCompleteRequest {

	@Size(max = 255, message = "유지보수 처리 내용은 255자 이하여야 합니다.")
	private String maintenanceResult;

	@DecimalMin(value = "0.00", message = "유지보수 비용은 0원 이상이어야 합니다.")
	private BigDecimal maintenanceCost;
}
