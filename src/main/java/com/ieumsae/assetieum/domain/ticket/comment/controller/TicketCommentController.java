package com.ieumsae.assetieum.domain.ticket.comment.controller;

import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentCreateRequest;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentDeleteResponse;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentResponse;
import com.ieumsae.assetieum.domain.ticket.comment.dto.TicketCommentUpdateRequest;
import com.ieumsae.assetieum.domain.ticket.comment.service.TicketCommentService;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
@PreAuthorize("isAuthenticated()")
public class TicketCommentController {

	private final TicketCommentService ticketCommentService;

	@PostMapping
	public ApiResponse<TicketCommentResponse> createComment(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @RequestBody TicketCommentCreateRequest request
	) {
		TicketCommentResponse response = ticketCommentService.createComment(
			authenticatedMember,
			ticketId,
			request
		);

		return ApiResponse.ok("티켓 댓글 등록에 성공했습니다.", response);
	}

	@GetMapping
	public ApiResponse<PaginationResponse<TicketCommentResponse>> getComments(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@Valid @ModelAttribute PaginationRequest request
	) {
		PaginationResponse<TicketCommentResponse> response = ticketCommentService.getComments(
			authenticatedMember,
			ticketId,
			request
		);

		return ApiResponse.ok("티켓 댓글 목록 조회에 성공했습니다.", response);
	}

	@PatchMapping("/{commentId}")
	public ApiResponse<TicketCommentResponse> updateComment(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@PathVariable Long commentId,
		@Valid @RequestBody TicketCommentUpdateRequest request
	) {
		TicketCommentResponse response = ticketCommentService.updateComment(
			authenticatedMember,
			ticketId,
			commentId,
			request
		);

		return ApiResponse.ok("티켓 댓글 수정에 성공했습니다.", response);
	}

	@DeleteMapping("/{commentId}")
	public ApiResponse<TicketCommentDeleteResponse> deleteComment(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable UUID ticketId,
		@PathVariable Long commentId
	) {
		TicketCommentDeleteResponse response = ticketCommentService.deleteComment(
			authenticatedMember,
			ticketId,
			commentId
		);

		return ApiResponse.ok("티켓 댓글 삭제에 성공했습니다.", response);
	}
}
