package com.ieumsae.assetieum.domain.ticket.rental.repository;

import com.ieumsae.assetieum.domain.ticket.rental.entity.RentalTicket;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalTicketRepository extends JpaRepository<RentalTicket, UUID> {
}
