package com.ieumsae.assetieum.domain.ticket.purchaserequest.repository;

import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.DirectPurchaseResult;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectPurchaseResultRepository extends JpaRepository<DirectPurchaseResult, UUID> {

	boolean existsByPurchaseRequestTicket_Id(UUID ticketId);

	Optional<DirectPurchaseResult> findByIdAndCompany_Id(UUID ticketId, UUID companyId);
}
