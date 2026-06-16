package com.ieumsae.assetieum.domain.ticket.purchaserequest.repository;

import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRequestTicketRepository extends JpaRepository<PurchaseRequestTicket, UUID> {

    Optional<PurchaseRequestTicket> findByIdAndCompany_Id(UUID ticketId, UUID companyId);
}
