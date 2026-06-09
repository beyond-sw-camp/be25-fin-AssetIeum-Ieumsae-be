package com.ieumsae.assetieum.domain.ticket.repository;

import com.ieumsae.assetieum.domain.ticket.entity.Ticket;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
}
