package com.ieumsae.assetieum.domain.ticket.common.repository;

import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface TicketRepository extends JpaRepository<Ticket, UUID>, TicketRepositoryCustom {

	Optional<Ticket> findByIdAndCompany_IdAndDeletedAtIsNull(UUID ticketId, UUID companyId);

	boolean existsByCompany_IdAndRequester_IdAndTicketStatusInAndDeletedAtIsNull(
		UUID companyId,
		UUID requesterId,
		Collection<TicketStatus> ticketStatuses
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Ticket> findWithLockByIdAndCompany_IdAndDeletedAtIsNull(UUID ticketId, UUID companyId);
}
