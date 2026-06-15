package com.ieumsae.assetieum.domain.ticket.common.dto;

import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketSearchRequest extends PaginationRequest {

	private TicketStatus ticketStatus;

	private TicketType ticketType;

	private String keyword;

	private UUID departmentId;

	private UUID requesterId;
}
