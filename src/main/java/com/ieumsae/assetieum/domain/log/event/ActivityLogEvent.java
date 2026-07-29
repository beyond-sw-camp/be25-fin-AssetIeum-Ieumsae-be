package com.ieumsae.assetieum.domain.log.event;

import com.ieumsae.assetieum.domain.log.type.ActivityLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import java.util.UUID;

public record ActivityLogEvent(
	UUID memberId,
	ActivityLogAction action,
	LogSubjectType subjectType,
	UUID subjectId,
	String targetPath
) {
}
