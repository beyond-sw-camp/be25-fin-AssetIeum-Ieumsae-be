package com.ieumsae.assetieum.domain.ticket.common.repository;

import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

	Optional<Ticket> findByIdAndCompany_IdAndDeletedAtIsNull(UUID ticketId, UUID companyId);
}
