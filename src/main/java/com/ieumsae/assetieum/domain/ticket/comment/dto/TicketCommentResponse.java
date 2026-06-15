package com.ieumsae.assetieum.domain.ticket.comment.dto;

import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.ticket.comment.entity.TicketComment;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketCommentResponse {

	private final Long commentId;
	private final UUID ticketId;
	private final UUID writerId;
	private final String writerName;
	private final MemberRole writerRole;
	private final String content;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	public static TicketCommentResponse from(TicketComment comment) {
		return TicketCommentResponse.builder()
			.commentId(comment.getId())
			.ticketId(comment.getTicket().getId())
			.writerId(comment.getWriter().getId())
			.writerName(comment.getWriter().getName())
			.writerRole(comment.getWriter().getRole())
			.content(comment.getContent())
			.createdAt(comment.getCreatedAt())
			.updatedAt(comment.getUpdatedAt())
			.build();
	}
}
