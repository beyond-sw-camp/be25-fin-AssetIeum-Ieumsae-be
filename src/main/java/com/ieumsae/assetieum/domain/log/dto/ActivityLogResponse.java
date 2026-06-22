package com.ieumsae.assetieum.domain.log.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.log.entity.ActivityLog;
import com.ieumsae.assetieum.domain.log.type.ActivityLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"activityLogId",
	"createdAt",
	"actorId",
	"actorName",
	"actorMemberNo",
	"action",
	"subjectType",
	"subjectId",
	"detail"
})
public class ActivityLogResponse {

	private final Long activityLogId;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime createdAt;

	private final UUID actorId;

	private final String actorName;

	private final String actorMemberNo;

	private final ActivityLogAction action;

	private final LogSubjectType subjectType;

	private final UUID subjectId;

	private final String detail;

	public static ActivityLogResponse from(ActivityLog log) {
		return ActivityLogResponse.builder()
			.activityLogId(log.getId())
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

	private static String createDetail(ActivityLog log) {
		String subjectName = switch (log.getSubjectType()) {
			case COMPANY -> "회사";
			case DEPARTMENT -> "부서";
			case MEMBER -> "사용자";
			case TANGIBLE_ASSET -> "유형 자산";
			case INTANGIBLE_ASSET -> "무형 자산";
			case TANGIBLE_ASSET_ITEM -> "유형 자산 품목";
			case INTANGIBLE_ASSET_ITEM -> "무형 자산 품목";
			case TICKET -> "티켓";
			case PURCHASE_PLAN -> "구매계획";
			case BUDGET -> "예산";
			case HR_EVENT -> "HR 이벤트";
			case INSPECTION -> "점검";
			case SYSTEM -> "시스템";
		};

		return switch (log.getAction()) {
			case VIEW -> subjectName + " 상세화면을 조회했습니다.";
			case SEARCH -> subjectName + " 목록을 검색했습니다.";
			case LOGIN -> "로그인했습니다.";
		};
	}
}
