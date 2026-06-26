package com.ieumsae.assetieum.domain.ticket.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TicketRejectionRequest {

	@NotBlank(message = "반려 사유는 필수입니다.")
	@Size(max = 255, message = "반려 사유는 255자 이하여야 합니다.")
	private String rejectionReason;
}
