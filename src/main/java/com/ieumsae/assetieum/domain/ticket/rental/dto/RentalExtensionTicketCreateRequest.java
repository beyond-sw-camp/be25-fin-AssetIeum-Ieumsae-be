package com.ieumsae.assetieum.domain.ticket.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RentalExtensionTicketCreateRequest {

	@NotNull(message = "자산 배정 ID는 필수입니다.")
	private UUID assignmentId;

	@NotNull(message = "연장 요청 반납 예정 일시는 필수입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime requestedDueDate;

	@Size(max = 255, message = "연장 요청 사유는 255자 이하여야 합니다.")
	private String requestReason;
}
