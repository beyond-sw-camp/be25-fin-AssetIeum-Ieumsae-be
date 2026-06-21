package com.ieumsae.assetieum.domain.log.dto;

import com.ieumsae.assetieum.domain.log.type.AuditLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuditLogSearchRequest extends PaginationRequest {

	private AuditLogAction action;

	private LogSubjectType subjectType;

	private UUID subjectId;

	private String keyword;
}
