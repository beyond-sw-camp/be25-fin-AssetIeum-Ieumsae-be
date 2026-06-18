package com.ieumsae.assetieum.domain.ticket.assetrequest.service;

import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.assetrequest.repository.AssetRequestTicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketRequesterResolver;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetRequestTicketService {

	private final TicketRepository ticketRepository;
	private final AssetRequestTicketRepository assetRequestTicketRepository;
	private final TangibleAssetItemRepository tangibleAssetItemRepository;
	private final IntangibleAssetItemRepository intangibleAssetItemRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TicketRequesterResolver ticketRequesterResolver;

	@Transactional
	public AssetRequestTicketCreateResponse createAssetRequestTicket(
		AuthenticatedMember authenticatedMember,
		AssetRequestTicketCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Member approver = ticketApprovalResolver.resolveDepartmentApprover(requester);
		TangibleAssetItem tangibleAssetItem = null;
		IntangibleAssetItem intangibleAssetItem = null;

		if (request.getAssetType() == AssetType.TANGIBLE) {
			tangibleAssetItem = findTangibleAssetItem(request.getAssetItemId(), companyId);
		} else {
			intangibleAssetItem = findIntangibleAssetItem(
				request.getAssetItemId(),
				companyId
			);
		}

		Ticket ticket = ticketRepository.save(Ticket.createAssetRequest(
			requester.getCompany(),
			ticketNoGenerator.generate(companyId),
			requester,
			requester.getDepartment(),
			approver,
			normalize(request.getRequestReason())
		));

		AssetRequestTicket assetRequestTicket = assetRequestTicketRepository.save(
			AssetRequestTicket.createRequest(
				ticket,
				requester.getCompany(),
				request.getRequestedUsageType(),
				tangibleAssetItem,
				intangibleAssetItem,
				request.getQuantity()
			)
		);

		return AssetRequestTicketCreateResponse.from(
			ticket,
			assetRequestTicket,
			request.getAssetType(),
			request.getAssetItemId()
		);
	}

	public AssetRequestTicketDetailResponse getAssetRequestTicket(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		AssetRequestTicket assetRequestTicket = assetRequestTicketRepository
			.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		Ticket ticket = assetRequestTicket.getTicket();

		validateReadable(ticket, viewer);
		boolean requesterView = ticket.getRequester().getId().equals(viewer.getId());

		return AssetRequestTicketDetailResponse.from(
			ticket,
			assetRequestTicket,
			viewer.getRole(),
			requesterView,
			createActions(ticket, viewer)
		);
	}

	private TangibleAssetItem findTangibleAssetItem(UUID itemId, UUID companyId) {
		TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

		if (!item.getCompany().getId().equals(companyId)) {
			throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND);
		}
		return item;
	}

	private IntangibleAssetItem findIntangibleAssetItem(UUID itemId, UUID companyId) {
		IntangibleAssetItem item = intangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));

		if (!item.getCompany().getId().equals(companyId)) {
			throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND);
		}
		return item;
	}

	private void validateReadable(Ticket ticket, Member viewer) {
		if (ticket.getRequester().getId().equals(viewer.getId())
			|| ticket.getApprover().getId().equals(viewer.getId())) {
			return;
		}

		MemberRole role = viewer.getRole();
		if (role == MemberRole.ADMIN || role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM) {
			return;
		}

		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}

	private AssetRequestTicketDetailResponse.Actions createActions(Ticket ticket, Member viewer) {
		if (ticket.getRequester().getId().equals(viewer.getId())) {
			return noActions();
		}

		boolean departmentApprover = ticket.getApprover().getId().equals(viewer.getId());
		boolean requested = ticket.getTicketStatus() == TicketStatus.REQUESTED;
		boolean departmentApproved = ticket.getTicketStatus() == TicketStatus.DEPARTMENT_APPROVED;
		boolean assetAssignable = isAssetAssignable(ticket, viewer);
		boolean assignee = ticket.getAssignee() != null && ticket.getAssignee().getId().equals(viewer.getId());
		boolean assetRole = isAssetRole(viewer.getRole());

		return AssetRequestTicketDetailResponse.Actions.builder()
			.canApproveDepartment(departmentApprover && requested)
			.canRejectDepartment(departmentApprover && requested)
			.canAssignAsset(assetAssignable && ticket.getAssignee() == null && (requested || departmentApproved))
			.canApproveAsset(assetAssignable && departmentApproved && assignee)
			.canRejectAsset(assetAssignable && departmentApproved && assignee)
			.canChangeProcessingStatus(assetRole && isProcessingStatusChangeable(ticket.getTicketStatus()))
			.build();
	}

	private AssetRequestTicketDetailResponse.Actions noActions() {
		return AssetRequestTicketDetailResponse.Actions.builder()
			.canApproveDepartment(false)
			.canRejectDepartment(false)
			.canAssignAsset(false)
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
		return status == TicketStatus.IN_PROGRESS
			|| status == TicketStatus.COMPLETED
			|| status == TicketStatus.CANCELLED;
	}

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
