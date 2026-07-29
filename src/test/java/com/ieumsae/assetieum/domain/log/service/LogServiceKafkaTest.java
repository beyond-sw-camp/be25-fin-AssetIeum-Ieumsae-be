package com.ieumsae.assetieum.domain.log.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ieumsae.assetieum.domain.log.event.ActivityLogEvent;
import com.ieumsae.assetieum.domain.log.event.LogEventPublisher;
import com.ieumsae.assetieum.domain.log.repository.ActivityLogRepository;
import com.ieumsae.assetieum.domain.log.repository.AuditLogRepository;
import com.ieumsae.assetieum.domain.log.type.ActivityLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class LogServiceKafkaTest {

	private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
	private final ActivityLogRepository activityLogRepository = mock(ActivityLogRepository.class);
	private final MemberRepository memberRepository = mock(MemberRepository.class);
	private final LogEventPublisher logEventPublisher = mock(LogEventPublisher.class);
	private final LogService logService = new LogService(
		auditLogRepository,
		activityLogRepository,
		memberRepository,
		logEventPublisher
	);

	@Test
	void publishesActivityLogWithoutDatabaseLookupWhenKafkaIsEnabled() {
		ReflectionTestUtils.setField(logService, "kafkaLogEnabled", true);
		UUID memberId = UUID.randomUUID();
		UUID companyId = UUID.randomUUID();
		UUID subjectId = UUID.randomUUID();
		AuthenticatedMember member = authenticatedMember(memberId, companyId);

		logService.recordActivityLog(
			member,
			ActivityLogAction.VIEW,
			LogSubjectType.TICKET,
			subjectId,
			"/api/v1/tickets/" + subjectId,
			"Viewed ticket"
		);

		verify(logEventPublisher).publishActivityLog(
			companyId,
			memberId,
			ActivityLogAction.VIEW,
			LogSubjectType.TICKET,
			subjectId,
			"/api/v1/tickets/" + subjectId
		);
		verifyNoInteractions(memberRepository, activityLogRepository);
	}

	@Test
	void skipsAlreadyPersistedActivityLogEvent() {
		UUID eventId = UUID.randomUUID();
		when(activityLogRepository.existsByEventId(eventId)).thenReturn(true);
		ActivityLogEvent event = new ActivityLogEvent(
			UUID.randomUUID(),
			ActivityLogAction.SEARCH,
			LogSubjectType.TICKET,
			UUID.randomUUID(),
			"/api/v1/tickets"
		);

		logService.persistActivityLogEvent(eventId, UUID.randomUUID(), event);

		verify(activityLogRepository).existsByEventId(eventId);
		verifyNoInteractions(memberRepository, logEventPublisher);
	}

	private AuthenticatedMember authenticatedMember(UUID memberId, UUID companyId) {
		return new AuthenticatedMember(
			memberId,
			companyId,
			"EMP0001",
			"member",
			null,
			MemberRole.EMPLOYEE
		);
	}
}
