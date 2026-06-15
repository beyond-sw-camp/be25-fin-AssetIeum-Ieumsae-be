package com.ieumsae.assetieum.domain.ticket.common.repository;

import com.ieumsae.assetieum.domain.ticket.common.dto.TicketListItemResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketSearchRequest;
import com.ieumsae.assetieum.domain.ticket.common.dto.TicketStatisticsResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface TicketRepositoryCustom {

	Page<TicketListItemResponse> searchTickets(UUID companyId, TicketSearchRequest request);

	TicketStatisticsResponse getTicketStatistics(UUID companyId, UUID departmentId, UUID requesterId);
}
