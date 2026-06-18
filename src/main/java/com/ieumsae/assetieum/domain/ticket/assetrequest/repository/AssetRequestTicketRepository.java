package com.ieumsae.assetieum.domain.ticket.assetrequest.repository;

import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRequestTicketRepository extends JpaRepository<AssetRequestTicket, UUID> {

	Optional<AssetRequestTicket> findByIdAndCompany_IdAndDeletedAtIsNull(UUID ticketId, UUID companyId);
}
