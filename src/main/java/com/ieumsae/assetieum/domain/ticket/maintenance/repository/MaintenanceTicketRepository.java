package com.ieumsae.assetieum.domain.ticket.maintenance.repository;

import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.maintenance.entity.MaintenanceTicket;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, UUID> {

	List<MaintenanceTicket> findAllByCompany_IdAndCollectedAtBetween(UUID companyId, LocalDateTime start, LocalDateTime end);

	Optional<MaintenanceTicket> findByIdAndCompany_IdAndDeletedAtIsNull(UUID ticketId, UUID companyId);

	boolean existsByCompany_IdAndTangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
		UUID companyId,
		UUID tangibleAssetId,
		Collection<TicketStatus> ticketStatuses
	);
}
