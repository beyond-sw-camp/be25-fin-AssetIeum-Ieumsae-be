package com.ieumsae.assetieum.domain.dashboard.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HrLifecycleEventResponse {
	private UUID eventId;
	private String eventType;
	private String memberName;
	private String departmentName;
	private LocalDateTime eventDate;
	private long dDay;
	private String status;
}
