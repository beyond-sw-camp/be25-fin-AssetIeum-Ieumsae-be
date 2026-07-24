package com.ieumsae.assetieum.domain.log.event;

import com.ieumsae.assetieum.domain.log.type.AuditLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import java.util.UUID;

public record AuditLogEvent(
	UUID memberId,
	AuditLogAction action,
	LogSubjectType subjectType,
	UUID subjectId,
	String targetPath,
	String detail
) {
}
