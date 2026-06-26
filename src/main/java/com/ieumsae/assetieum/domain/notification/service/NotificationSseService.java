package com.ieumsae.assetieum.domain.notification.service;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.notification.dto.NotificationListItemResponse;
import com.ieumsae.assetieum.domain.notification.repository.NotificationEmitterRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationSseService {

	private static final long TIMEOUT_MILLIS = 60L * 60L * 1000L;

	private final NotificationEmitterRepository notificationEmitterRepository;
	private final MemberRepository memberRepository;

	public SseEmitter subscribe(AuthenticatedMember authenticatedMember) {
		Member receiver = findActiveMember(
			authenticatedMember.id(),
			authenticatedMember.companyId()
		);
		SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);

		notificationEmitterRepository.save(receiver.getId(), emitter);
		emitter.onCompletion(() -> notificationEmitterRepository.delete(receiver.getId(), emitter));
		emitter.onTimeout(() -> notificationEmitterRepository.delete(receiver.getId(), emitter));
		emitter.onError(exception -> notificationEmitterRepository.delete(receiver.getId(), emitter));

		sendConnectEvent(receiver.getId(), emitter);

		return emitter;
	}

	public void send(UUID receiverId, NotificationListItemResponse response) {
		for (SseEmitter emitter : notificationEmitterRepository.findAllByReceiverId(receiverId)) {
			sendNotificationEvent(receiverId, emitter, response);
		}
	}

	private Member findActiveMember(UUID memberId, UUID companyId) {
		Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}
		return member;
	}

	private void sendConnectEvent(UUID receiverId, SseEmitter emitter) {
		try {
			emitter.send(SseEmitter.event()
				.name("connect")
				.data("connected"));
		} catch (IOException exception) {
			notificationEmitterRepository.delete(receiverId, emitter);
			emitter.completeWithError(exception);
		}
	}

	private void sendNotificationEvent(
		UUID receiverId,
		SseEmitter emitter,
		NotificationListItemResponse response
	) {
		try {
			emitter.send(SseEmitter.event()
				.name("notification")
				.id(String.valueOf(response.getNotificationId()))
				.data(response));
		} catch (IOException exception) {
			notificationEmitterRepository.delete(receiverId, emitter);
			emitter.completeWithError(exception);
		}
	}
}
