package com.ieumsae.assetieum.domain.ticket.purchasereturn.repository;

import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.entity.PurchaseReturnTicket;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseReturnTicketRepository extends JpaRepository<PurchaseReturnTicket, UUID> {

	boolean existsByCompany_IdAndTangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
		UUID companyId,
		UUID tangibleAssetId,
		Collection<TicketStatus> ticketStatuses
	);

	boolean existsByCompany_IdAndIntangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
		UUID companyId,
		UUID intangibleAssetId,
		Collection<TicketStatus> ticketStatuses
	);
}
