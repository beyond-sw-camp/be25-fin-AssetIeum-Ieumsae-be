package com.ieumsae.assetieum.domain.ticket.common.repository;

import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface TicketRepository extends JpaRepository<Ticket, UUID>, TicketRepositoryCustom {

	Optional<Ticket> findByIdAndCompany_IdAndDeletedAtIsNull(UUID ticketId, UUID companyId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Ticket> findWithLockByIdAndCompany_IdAndDeletedAtIsNull(UUID ticketId, UUID companyId);
}
