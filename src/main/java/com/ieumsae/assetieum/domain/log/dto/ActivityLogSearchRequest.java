package com.ieumsae.assetieum.domain.log.dto;

import com.ieumsae.assetieum.domain.log.type.ActivityLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivityLogSearchRequest extends PaginationRequest {

	private ActivityLogAction action;

	private LogSubjectType subjectType;

	private UUID subjectId;

	private String keyword;
}
