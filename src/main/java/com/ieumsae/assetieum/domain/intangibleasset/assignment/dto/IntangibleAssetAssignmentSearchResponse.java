package com.ieumsae.assetieum.domain.intangibleasset.assignment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
        "assignmentId",
        "memberId",
        "memberName",
        "memberNo",
        "departmentId",
        "departmentName",
        "assignedAt",
        "endedAt",
        "assignmentStatus"
})
public class IntangibleAssetAssignmentSearchResponse {

    private UUID assignmentId;

    private String memberName;

    private String memberNo;

    private String departmentName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime assignedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endedAt;

    private AssignmentStatus assignmentStatus;

    public static IntangibleAssetAssignmentSearchResponse from(IntangibleAssetAssignment assignment) {
        return IntangibleAssetAssignmentSearchResponse.builder()
                .assignmentId(assignment.getId())
                .memberName(assignment.getMember().getName())
                .memberNo(assignment.getMember().getMemberNo())
                .departmentName(assignment.getDepartment().getName())
                .assignedAt(assignment.getAssignedAt())
                .endedAt(assignment.getEndedAt())
                .assignmentStatus(assignment.getAssignmentStatus())
                .build();
    }
}