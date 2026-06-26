package com.ieumsae.assetieum.domain.tangibleasset.assignment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({
        "assignmentId",
        "memberName",
        "memberNo",
        "departmentName",
        "assignmentType",
        "assignedAt",
        "endedAt",
        "assignmentStatus"
})
public class TangibleAssetAssignmentResponse {

    private UUID assignmentId;

    private String memberName;

    private String memberNo;

    private String departmentName;

    private UsageType assignmentType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime assignedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endedAt;

    private AssignmentStatus assignmentStatus;

    public static TangibleAssetAssignmentResponse from(TangibleAssetAssignment assignment) {
        return TangibleAssetAssignmentResponse.builder()
                .assignmentId(assignment.getId())
                .memberName(assignment.getMember().getName())
                .memberNo(assignment.getMember().getMemberNo())
                .departmentName(assignment.getDepartment().getName())
                .assignmentType(assignment.getAssignmentType())
                .assignedAt(assignment.getAssignedAt())
                .endedAt(assignment.getEndedAt())
                .assignmentStatus(assignment.getAssignmentStatus())
                .build();
    }
}
