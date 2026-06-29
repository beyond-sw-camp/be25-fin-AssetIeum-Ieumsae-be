package com.ieumsae.assetieum.domain.notification.service;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.notification.dto.NotificationListItemResponse;
import com.ieumsae.assetieum.domain.notification.dto.NotificationReadAllResponse;
import com.ieumsae.assetieum.domain.notification.dto.NotificationReadResponse;
import com.ieumsae.assetieum.domain.notification.dto.NotificationUnreadCountResponse;
import com.ieumsae.assetieum.domain.notification.entity.Notification;
import com.ieumsae.assetieum.domain.notification.repository.NotificationRepository;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;
	private final NotificationSsePublisher notificationSsePublisher;

	public PaginationResponse<NotificationListItemResponse> getNotifications(
		AuthenticatedMember authenticatedMember,
		PaginationRequest request
	) {
		Member receiver = findActiveMember(authenticatedMember.id());

		return PaginationResponse.from(
			notificationRepository.findAllByReceiver_IdAndCompany_Id(
				receiver.getId(),
				receiver.getCompany().getId(),
				request.toPageable()
			).map(NotificationListItemResponse::from)
		);
	}

	public NotificationUnreadCountResponse getUnreadCount(AuthenticatedMember authenticatedMember) {
		Member receiver = findActiveMember(authenticatedMember.id());
		long unreadCount = notificationRepository.countByReceiver_IdAndCompany_IdAndIsReadFalse(
			receiver.getId(),
			receiver.getCompany().getId()
		);

		return NotificationUnreadCountResponse.from(unreadCount);
	}

	@Transactional
	public NotificationReadResponse markAsRead(
		AuthenticatedMember authenticatedMember,
		Long notificationId
	) {
		Member receiver = findActiveMember(authenticatedMember.id());
		Notification notification = findNotification(
			notificationId,
			receiver.getId(),
			receiver.getCompany().getId()
		);

		notification.markAsRead();

		return NotificationReadResponse.from(notification.getId());
	}

	@Transactional
	public NotificationReadAllResponse markAllAsRead(AuthenticatedMember authenticatedMember) {
		Member receiver = findActiveMember(authenticatedMember.id());
		int updatedCount = notificationRepository.markAllAsRead(
			receiver.getId(),
			receiver.getCompany().getId()
		);

		return NotificationReadAllResponse.from(updatedCount);
	}

	@Transactional
	public void createNotification(
		Member receiver,
		NotificationType notificationType,
		String title,
		String content,
		NotificationTargetType targetType,
		UUID targetId
	) {
		Notification notification = notificationRepository.saveAndFlush(Notification.create(
			receiver.getCompany(),
			receiver,
			notificationType,
			title,
			content,
			targetType,
			targetId
		));
		NotificationListItemResponse response = NotificationListItemResponse.from(notification);

		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			notificationSsePublisher.publish(receiver.getId(), response);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				notificationSsePublisher.publish(receiver.getId(), response);
			}
		});
	}

	private Member findActiveMember(UUID memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}
		return member;
	}

	private Member findActiveMember(UUID memberId, UUID companyId) {
		Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}
		return member;
	}

	private Notification findNotification(Long notificationId, UUID receiverId, UUID companyId) {
		return notificationRepository.findByIdAndReceiver_IdAndCompany_Id(
				notificationId,
				receiverId,
				companyId
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
	}
}
