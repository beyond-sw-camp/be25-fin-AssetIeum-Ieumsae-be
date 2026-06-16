package com.ieumsae.assetieum.domain.ticket.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentApprovalResponse {

	private final UUID ticketId;
	private final String ticketNo;
	private final TicketStatus ticketStatus;
	private final UUID departmentApproverId;
	private final String departmentApproverName;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime departmentApprovedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime departmentRejectedAt;
	private final String departmentRejectionReason;

	public static DepartmentApprovalResponse from(Ticket ticket) {
		return DepartmentApprovalResponse.builder()
			.ticketId(ticket.getId())
			.ticketNo(ticket.getTicketNo())
			.ticketStatus(ticket.getTicketStatus())
			.departmentApproverId(ticket.getApprover().getId())
			.departmentApproverName(ticket.getApprover().getName())
			.departmentApprovedAt(ticket.getDepartmentApprovedAt())
			.departmentRejectedAt(ticket.getDepartmentRejectedAt())
			.departmentRejectionReason(ticket.getDepartmentRejectionReason())
			.build();
	}
}
