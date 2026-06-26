package com.ieumsae.assetieum.domain.ticket.common.dto;

import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TicketProcessingStatusUpdateRequest {

	@NotNull
	private TicketStatus ticketStatus;
}
