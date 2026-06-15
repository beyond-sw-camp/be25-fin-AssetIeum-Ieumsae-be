package com.ieumsae.assetieum.domain.ticket.rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
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
public class RentalTicketCreateRequest {

	@NotNull(message = "요청 사용 유형은 필수입니다.")
	private RequestedUsageType requestedUsageType;

	@NotNull(message = "유형 자산 품목 ID는 필수입니다.")
	private UUID tangibleAssetItemId;

	@NotNull(message = "대여 시작 일시는 필수입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime rentalStartDate;

	@NotNull(message = "요청 반납 예정 일시는 필수입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime requestedDueDate;

	@Size(max = 255, message = "대여 목적 사유는 255자 이하여야 합니다.")
	private String requestReason;
}
