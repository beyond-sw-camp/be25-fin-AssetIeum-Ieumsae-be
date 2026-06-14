package com.ieumsae.assetieum.domain.ticket.comment.repository;

import com.ieumsae.assetieum.domain.ticket.comment.entity.TicketComment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

	Page<TicketComment> findAllByTicket_IdAndCompany_IdAndDeletedAtIsNull(
		UUID ticketId,
		UUID companyId,
		Pageable pageable
	);

	Optional<TicketComment> findByIdAndTicket_IdAndCompany_IdAndDeletedAtIsNull(
		Long commentId,
		UUID ticketId,
		UUID companyId
	);
}
