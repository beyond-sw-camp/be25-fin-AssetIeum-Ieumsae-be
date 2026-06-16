package com.ieumsae.assetieum.domain.ticket.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketCommentDeleteResponse {

	private final Long commentId;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime deletedAt;

	public static TicketCommentDeleteResponse from(Long commentId, LocalDateTime deletedAt) {
		return TicketCommentDeleteResponse.builder()
			.commentId(commentId)
			.deletedAt(deletedAt)
			.build();
	}
}
