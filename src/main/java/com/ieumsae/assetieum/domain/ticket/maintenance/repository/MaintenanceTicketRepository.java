package com.ieumsae.assetieum.domain.ticket.maintenance.repository;

import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.maintenance.entity.MaintenanceTicket;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, UUID> {

	boolean existsByCompany_IdAndTangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
		UUID companyId,
		UUID tangibleAssetId,
		Collection<TicketStatus> ticketStatuses
	);
}
