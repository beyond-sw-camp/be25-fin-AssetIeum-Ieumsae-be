package com.ieumsae.assetieum.domain.ticket.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceTicketCreateRequest {

	@NotNull(message = "자산 배정 ID는 필수입니다.")
	private UUID assignmentId;

	@NotBlank(message = "요청 상세는 필수입니다.")
	@Size(max = 255, message = "요청 상세는 255자 이하여야 합니다.")
	private String requestDetail;
}
