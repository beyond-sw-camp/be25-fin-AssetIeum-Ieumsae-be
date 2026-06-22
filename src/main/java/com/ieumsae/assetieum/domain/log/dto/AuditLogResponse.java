package com.ieumsae.assetieum.domain.log.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.log.entity.AuditLog;
import com.ieumsae.assetieum.domain.log.type.AuditLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"auditLogId",
	"createdAt",
	"actorId",
	"actorName",
	"actorMemberNo",
	"action",
	"subjectType",
	"subjectId",
	"detail"
})
public class AuditLogResponse {

	private final Long auditLogId;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime createdAt;

	private final UUID actorId;

	private final String actorName;

	private final String actorMemberNo;

	private final AuditLogAction action;

	private final LogSubjectType subjectType;

	private final UUID subjectId;

	private final String detail;

	public static AuditLogResponse from(AuditLog log) {
		return AuditLogResponse.builder()
			.auditLogId(log.getId())
			.createdAt(log.getCreatedAt())
			.actorId(log.getMember().getId())
			.actorName(log.getMember().getName())
			.actorMemberNo(log.getMember().getMemberNo())
			.action(log.getAction())
			.subjectType(log.getSubjectType())
			.subjectId(log.getSubjectId())
			.detail(createDetail(log))
			.build();
	}

	private static String createDetail(AuditLog log) {
		if ("-".equals(log.getBeforeValue())) {
			return log.getAfterValue();
		}
		return log.getBeforeValue() + " -> " + log.getAfterValue();
	}
}
