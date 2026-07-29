package com.ieumsae.assetieum.domain.log.event;

import com.ieumsae.assetieum.domain.log.type.ActivityLogAction;
import com.ieumsae.assetieum.domain.log.type.AuditLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import com.ieumsae.assetieum.global.kafka.outbox.OutboxService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogEventPublisher {

	private static final String ACTIVITY_LOG_EVENT_TYPE = "ACTIVITY_LOG_CREATED";
	private static final String AUDIT_LOG_EVENT_TYPE = "AUDIT_LOG_CREATED";

	private final OutboxService outboxService;

	@Value("${app.kafka.topics.activity-log}")
	private String activityLogTopic;

	@Value("${app.kafka.topics.audit-log}")
	private String auditLogTopic;

	public void publishActivityLog(
		UUID companyId,
		UUID memberId,
		ActivityLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String targetPath
	) {
		EventEnvelope<ActivityLogEvent> event = EventEnvelope.of(
			ACTIVITY_LOG_EVENT_TYPE,
			companyId,
			new ActivityLogEvent(memberId, action, subjectType, subjectId, targetPath)
		);
		publish(activityLogTopic, memberId, event);
	}

	public void publishAuditLog(
		UUID companyId,
		UUID memberId,
		AuditLogAction action,
		LogSubjectType subjectType,
		UUID subjectId,
		String targetPath,
		String detail
	) {
		EventEnvelope<AuditLogEvent> event = EventEnvelope.of(
			AUDIT_LOG_EVENT_TYPE,
			companyId,
			new AuditLogEvent(memberId, action, subjectType, subjectId, targetPath, detail)
		);
		publish(auditLogTopic, memberId, event);
	}

	private void publish(String topic, UUID key, EventEnvelope<?> event) {
		outboxService.enqueue(topic, key.toString(), event);
	}
}
