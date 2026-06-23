package com.ieumsae.assetieum.domain.ticket.common.repository;

import com.ieumsae.assetieum.domain.ticket.common.entity.TicketAssignmentTarget;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketAssignmentTargetRepository extends JpaRepository<TicketAssignmentTarget, UUID> {

	List<TicketAssignmentTarget> findAllByTicket_IdAndCompany_IdOrderByCreatedAtAsc(UUID ticketId, UUID companyId);

	void deleteAllByTicket_IdAndCompany_Id(UUID ticketId, UUID companyId);
}
