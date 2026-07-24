package com.ieumsae.assetieum.domain.notification.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.notification.event.NotificationCreatedEvent;
import com.ieumsae.assetieum.domain.notification.event.NotificationEventPublisher;
import com.ieumsae.assetieum.domain.notification.repository.NotificationRepository;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class NotificationServiceKafkaTest {

	private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
	private final MemberRepository memberRepository = mock(MemberRepository.class);
	private final TicketRepository ticketRepository = mock(TicketRepository.class);
	private final NotificationSsePublisher ssePublisher = mock(NotificationSsePublisher.class);
	private final NotificationEventPublisher eventPublisher = mock(NotificationEventPublisher.class);
	private final NotificationService service = new NotificationService(
		notificationRepository,
		memberRepository,
		ticketRepository,
		ssePublisher,
		eventPublisher
	);

	@Test
	void publishesEventWithoutWritingNotificationInRequestThread() {
		ReflectionTestUtils.setField(service, "kafkaNotificationEnabled", true);
		Member receiver = mock(Member.class);
		Company company = mock(Company.class);
		UUID receiverId = UUID.randomUUID();
		UUID companyId = UUID.randomUUID();
		UUID targetId = UUID.randomUUID();
		when(receiver.getId()).thenReturn(receiverId);
		when(receiver.getCompany()).thenReturn(company);
		when(company.getId()).thenReturn(companyId);

		service.createNotification(
			receiver,
			NotificationType.TICKET_STATUS_CHANGED,
			"title",
			"content",
			NotificationTargetType.TICKET,
			targetId
		);

		verify(eventPublisher).publish(
			receiver,
			NotificationType.TICKET_STATUS_CHANGED,
			"title",
			"content",
			NotificationTargetType.TICKET,
			targetId
		);
		verifyNoInteractions(notificationRepository, ssePublisher);
	}

	@Test
	void skipsAlreadyPersistedNotificationEvent() {
		UUID eventId = UUID.randomUUID();
		when(notificationRepository.existsByEventId(eventId)).thenReturn(true);
		NotificationCreatedEvent event = new NotificationCreatedEvent(
			UUID.randomUUID(),
			NotificationType.TICKET_STATUS_CHANGED,
			"title",
			"content",
			NotificationTargetType.TICKET,
			UUID.randomUUID()
		);

		service.persistNotificationEvent(eventId, UUID.randomUUID(), event);

		verify(notificationRepository).existsByEventId(eventId);
		verifyNoInteractions(memberRepository, eventPublisher, ssePublisher);
	}
}
