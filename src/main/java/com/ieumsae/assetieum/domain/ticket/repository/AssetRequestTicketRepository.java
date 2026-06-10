package com.ieumsae.assetieum.domain.ticket.repository;

import com.ieumsae.assetieum.domain.ticket.entity.AssetRequestTicket;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRequestTicketRepository extends JpaRepository<AssetRequestTicket, UUID> {
}
