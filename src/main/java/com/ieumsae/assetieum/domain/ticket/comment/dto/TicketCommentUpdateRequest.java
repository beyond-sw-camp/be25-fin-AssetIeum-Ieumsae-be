package com.ieumsae.assetieum.domain.ticket.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketCommentUpdateRequest {

	@NotBlank(message = "댓글 내용은 필수입니다.")
	@Size(max = 255, message = "댓글 내용은 255자 이하여야 합니다.")
	private String content;
}
