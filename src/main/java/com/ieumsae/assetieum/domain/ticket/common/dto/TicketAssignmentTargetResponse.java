package com.ieumsae.assetieum.domain.ticket.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.common.entity.TicketAssignmentTarget;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketAssignmentTargetStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketAssignmentTargetResponse {

	private final UUID targetId;
	private final UUID memberId;
	private final String memberNo;
	private final String memberName;
	private final UUID departmentId;
	private final String departmentName;
	private final TicketAssignmentTargetStatus status;
	private final AssetType assignedAssetType;
	private final UUID assignedAssetId;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime assignedAt;

	public static TicketAssignmentTargetResponse from(TicketAssignmentTarget target) {
		return TicketAssignmentTargetResponse.builder()
			.targetId(target.getId())
			.memberId(target.getMember().getId())
			.memberNo(target.getMember().getMemberNo())
			.memberName(target.getMember().getName())
			.departmentId(target.getMember().getDepartment().getId())
			.departmentName(target.getMember().getDepartment().getName())
			.status(target.getStatus())
			.assignedAssetType(target.getAssignedAssetType())
			.assignedAssetId(target.getAssignedAssetId())
			.assignedAt(target.getAssignedAt())
			.build();
	}
}
