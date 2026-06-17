package com.ieumsae.assetieum.domain.hr.hrevent.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventStatus;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@JsonPropertyOrder({
        "hrEventId",
        "hrEventNo",
        "departmentId",
        "departmentName",
        "memberId",
        "memberName",
        "hrEventStatus",
        "hrEventType",
        "eventDate",
        "executedAt",
        "completedAt",
        "cancelledAt",
        "createdAt",
        "updatedAt",
})
public class HrEventResponse {

    private UUID hrEventId;

    private String hrEventNo;

    private UUID departmentId;

    private String departmentName;

    private UUID memberId;

    private String memberName;

    private HrEventStatus hrEventStatus;

    private HrEventType hrEventType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime executedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cancelledAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static HrEventResponse from(HrEvent hrEvent) {
        return HrEventResponse.builder()
                .hrEventId(hrEvent.getId())
                .hrEventNo(hrEvent.getHrEventNo())
                .departmentId(hrEvent.getDepartment().getId())
                .departmentName(hrEvent.getDepartment().getName())
                .memberId(hrEvent.getMember().getId())
                .memberName(hrEvent.getMember().getName())
                .hrEventStatus(hrEvent.getHrEventStatus())
                .hrEventType(hrEvent.getEventType())
                .eventDate(hrEvent.getEventDate())
                .executedAt(hrEvent.getExecutedAt())
                .completedAt(hrEvent.getCompletedAt())
                .cancelledAt(hrEvent.getCancelledAt())
                .createdAt(hrEvent.getCreatedAt())
                .updatedAt(hrEvent.getUpdatedAt())
                .build();
    }
}