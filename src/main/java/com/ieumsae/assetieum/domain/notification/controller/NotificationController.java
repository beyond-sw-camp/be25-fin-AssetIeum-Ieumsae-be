package com.ieumsae.assetieum.domain.notification.controller;

import com.ieumsae.assetieum.domain.notification.dto.NotificationListItemResponse;
import com.ieumsae.assetieum.domain.notification.dto.NotificationReadAllResponse;
import com.ieumsae.assetieum.domain.notification.dto.NotificationReadResponse;
import com.ieumsae.assetieum.domain.notification.dto.NotificationUnreadCountResponse;
import com.ieumsae.assetieum.domain.notification.service.NotificationService;
import com.ieumsae.assetieum.domain.notification.service.NotificationSseService;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.response.ApiResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

	private final NotificationService notificationService;
	private final NotificationSseService notificationSseService;

	@GetMapping
	public ApiResponse<PaginationResponse<NotificationListItemResponse>> getNotifications(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@Valid @ModelAttribute PaginationRequest request
	) {
		PaginationResponse<NotificationListItemResponse> response = notificationService.getNotifications(
			authenticatedMember,
			request
		);
		return ApiResponse.ok("알림 목록 조회가 성공했습니다.", response);
	}

	@GetMapping("/unread-count")
	public ApiResponse<NotificationUnreadCountResponse> getUnreadCount(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		NotificationUnreadCountResponse response = notificationService.getUnreadCount(authenticatedMember);
		return ApiResponse.ok("읽지 않은 알림 수 조회가 성공했습니다.", response);
	}

	@GetMapping("/subscribe")
	public SseEmitter subscribe(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		return notificationSseService.subscribe(authenticatedMember);
	}

	@PatchMapping("/{notificationId}/read")
	public ApiResponse<NotificationReadResponse> markAsRead(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember,
		@PathVariable Long notificationId
	) {
		NotificationReadResponse response = notificationService.markAsRead(authenticatedMember, notificationId);
		return ApiResponse.ok("알림 읽음 처리가 성공했습니다.", response);
	}

	@PatchMapping("/read-all")
	public ApiResponse<NotificationReadAllResponse> markAllAsRead(
		@AuthenticationPrincipal AuthenticatedMember authenticatedMember
	) {
		NotificationReadAllResponse response = notificationService.markAllAsRead(authenticatedMember);
		return ApiResponse.ok("전체 알림 읽음 처리가 성공했습니다.", response);
	}
}

