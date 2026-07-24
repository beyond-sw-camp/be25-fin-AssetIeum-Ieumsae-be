package com.ieumsae.assetieum.domain.ticket.comment.service;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentCreateRequest;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentDeleteResponse;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentResponse;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentUpdateRequest;
import com.ieumsae.assetieum.domain.ticket.comment.entity.TicketComment;
import com.ieumsae.assetieum.domain.ticket.comment.event.TicketCommentEventPublisher;
import com.ieumsae.assetieum.domain.ticket.comment.repository.TicketCommentRepository;
import com.ieumsae.assetieum.domain.ticket.comment.type.TicketCommentEventType;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketCommentService {

	private final TicketCommentRepository ticketCommentRepository;
	private final TicketRepository ticketRepository;
	private final MemberRepository memberRepository;
	private final TicketCommentEventPublisher ticketCommentEventPublisher;

	@Transactional
	public TicketCommentResponse createComment(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		TicketCommentCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member writer = findActiveMember(authenticatedMember.id(), companyId);
		Ticket ticket = findActiveTicket(ticketId, companyId);

		TicketComment comment = ticketCommentRepository.save(TicketComment.create(
			writer.getCompany(),
			ticket,
			writer,
			request.getContent().trim()
		));

		TicketCommentResponse response = TicketCommentResponse.from(comment);
		ticketCommentEventPublisher.publish(
			companyId, ticketId, TicketCommentEventType.CREATED, response
		);
		return response;
	}

	public PaginationResponse<TicketCommentResponse> getComments(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		PaginationRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		findActiveMember(authenticatedMember.id(), companyId);
		findActiveTicket(ticketId, companyId);

		Page<TicketCommentResponse> comments = ticketCommentRepository
			.findAllByTicket_IdAndCompany_IdAndDeletedAtIsNull(
				ticketId,
				companyId,
				PageRequest.of(request.getPage(), request.getSize(), Sort.by(Sort.Direction.ASC, "createdAt"))
			)
			.map(TicketCommentResponse::from);

		return PaginationResponse.from(comments);
	}

	@Transactional
	public TicketCommentResponse updateComment(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		Long commentId,
		TicketCommentUpdateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = findActiveMember(authenticatedMember.id(), companyId);
		findActiveTicket(ticketId, companyId);
		TicketComment comment = findActiveComment(commentId, ticketId, companyId);
		validateCommentEditable(requester, comment);

		comment.updateContent(request.getContent().trim());

		TicketCommentResponse response = TicketCommentResponse.from(comment);
		ticketCommentEventPublisher.publish(
			companyId, ticketId, TicketCommentEventType.UPDATED, response
		);
		return response;
	}

	@Transactional
	public TicketCommentDeleteResponse deleteComment(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		Long commentId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = findActiveMember(authenticatedMember.id(), companyId);
		findActiveTicket(ticketId, companyId);
		TicketComment comment = findActiveComment(commentId, ticketId, companyId);
		validateCommentEditable(requester, comment);

		LocalDateTime deletedAt = comment.delete();

		TicketCommentDeleteResponse response = TicketCommentDeleteResponse.from(comment.getId(), deletedAt);
		ticketCommentEventPublisher.publish(
			companyId, ticketId, TicketCommentEventType.DELETED, response
		);
		return response;
	}

	private Member findActiveMember(UUID memberId, UUID companyId) {
		Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}
		return member;
	}

	private Ticket findActiveTicket(UUID ticketId, UUID companyId) {
		return ticketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
	}

	private TicketComment findActiveComment(Long commentId, UUID ticketId, UUID companyId) {
		return ticketCommentRepository.findByIdAndTicket_IdAndCompany_IdAndDeletedAtIsNull(
				commentId,
				ticketId,
				companyId
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_COMMENT_NOT_FOUND));
	}

	private void validateCommentEditable(Member requester, TicketComment comment) {
		if (comment.getWriter().getId().equals(requester.getId()) || requester.getRole() == MemberRole.ADMIN) {
			return;
		}

		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}
}
