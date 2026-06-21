package com.ieumsae.assetieum.domain.ticket.purchaserequest.service;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketDetailResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseRequestActionResolver {

	private final TicketApprovalResolver ticketApprovalResolver;

	public void validateReadable(Ticket ticket, Member viewer) {
		if (ticket.getRequester().getId().equals(viewer.getId())
			|| ticket.getApprover().getId().equals(viewer.getId())) {
			return;
		}

		if (isAssetRole(viewer.getRole())) {
			return;
		}

		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}

	public PurchaseRequestTicketDetailResponse.Actions createActions(Ticket ticket, Member viewer) {
		if (ticket.getRequester().getId().equals(viewer.getId())) {
			return noActions();
		}

		boolean departmentApprover = ticket.getApprover().getId().equals(viewer.getId());
		boolean requested = ticket.getTicketStatus() == TicketStatus.REQUESTED;
		boolean departmentApproved = ticket.getTicketStatus() == TicketStatus.DEPARTMENT_APPROVED;
		boolean assetAssignable = isAssetAssignable(ticket, viewer);
		boolean assignee = ticket.getAssignee() != null && ticket.getAssignee().getId().equals(viewer.getId());
		boolean assetRole = isAssetRole(viewer.getRole());

		return PurchaseRequestTicketDetailResponse.Actions.builder()
			.canApproveDepartment(departmentApprover && requested)
			.canRejectDepartment(departmentApprover && requested)
			.canApproveAsset(assetAssignable && departmentApproved && assignee)
			.canRejectAsset(assetAssignable && departmentApproved && assignee)
			.canChangeProcessingStatus(assetRole && isProcessingStatusChangeable(ticket.getTicketStatus()))
			.build();
	}

	private PurchaseRequestTicketDetailResponse.Actions noActions() {
		return PurchaseRequestTicketDetailResponse.Actions.builder()
			.canApproveDepartment(false)
			.canRejectDepartment(false)
			.canApproveAsset(false)
			.canRejectAsset(false)
			.canChangeProcessingStatus(false)
			.build();
	}

	private boolean isAssetAssignable(Ticket ticket, Member member) {
		MemberRole role = member.getRole();
		if (ticketApprovalResolver.requiresAdminAssetApproval(ticket)) {
			return role == MemberRole.ADMIN;
		}
		if (ticketApprovalResolver.requiresAssetManagerApproval(ticket)) {
			return role == MemberRole.ASSET_MANAGER;
		}
		return role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM;
	}

	private boolean isAssetRole(MemberRole role) {
		return role == MemberRole.ADMIN || role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM;
	}

	private boolean isProcessingStatusChangeable(TicketStatus status) {
		return status == TicketStatus.ASSET_APPROVED || status == TicketStatus.IN_PROGRESS;
	}
}
