package com.ieumsae.assetieum.domain.ticket.rental.repository;

import com.ieumsae.assetieum.domain.ticket.rental.entity.RentalTicket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalTicketRepository extends JpaRepository<RentalTicket, UUID> {

	boolean existsByCompany_IdAndTangibleAsset_IdAndTicket_TicketTypeAndTicket_TicketStatusInAndDeletedAtIsNull(
		UUID companyId,
		UUID tangibleAssetId,
		TicketType ticketType,
		Collection<TicketStatus> ticketStatuses
	);
}
