package com.ieumsae.assetieum.domain.ticket.purchaserequest.repository;

import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRequestTicketRepository extends JpaRepository<PurchaseRequestTicket, UUID> {

    Optional<PurchaseRequestTicket> findByIdAndCompany_Id(UUID ticketId, UUID companyId);

    boolean existsByCompany_IdAndRequestMethodAndDeletedAtIsNullAndTicket_TicketStatusIn(
            UUID companyId,
            RequestMethod requestMethod,
            Collection<TicketStatus> ticketStatuses
    );
}
