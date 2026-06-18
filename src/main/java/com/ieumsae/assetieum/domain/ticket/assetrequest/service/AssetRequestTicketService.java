package com.ieumsae.assetieum.domain.ticket.assetrequest.service;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.AssetUsageType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignableItemResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignableItemSearchRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignableItemsResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.assetrequest.repository.AssetRequestTicketRepository;
import com.ieumsae.assetieum.domain.ticket.assetrequest.type.AssetRequestTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketRequesterResolver;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
	private final TangibleAssetRepository tangibleAssetRepository;
	private final IntangibleAssetRepository intangibleAssetRepository;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
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

	public AssetRequestAssignableItemsResponse getAssignableItems(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		AssetRequestAssignableItemSearchRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		AssetRequestTicket assetRequestTicket = findAssetRequestTicket(ticketId, companyId);
		Ticket ticket = assetRequestTicket.getTicket();

		validateReadable(ticket, viewer);
		AssetType assetType = resolveSearchAssetType(assetRequestTicket, request);
		AssetRequestAssignableItemResponse requestedItem = getRequestedItem(assetRequestTicket, assetType);

		if (assetType == AssetType.TANGIBLE) {
			Page<AssetRequestAssignableItemResponse> items = tangibleAssetItemRepository
				.searchAssignableItems(
					companyId,
					request.getCategoryId(),
					normalize(request.getKeyword()),
					request.toPageable()
				)
				.map(item -> AssetRequestAssignableItemResponse.from(
					item,
					requestedItem.getItemId(),
					getAvailableTangibleCount(companyId, item.getId())
				));
			return AssetRequestAssignableItemsResponse.builder()
				.requestedItem(requestedItem)
				.items(PaginationResponse.from(items))
				.build();
		}

		Page<AssetRequestAssignableItemResponse> items = intangibleAssetItemRepository
			.searchAssignableItems(
				companyId,
				request.getCategoryId(),
				normalize(request.getKeyword()),
				request.toPageable()
			)
			.map(item -> AssetRequestAssignableItemResponse.from(
				item,
				requestedItem.getItemId(),
				getAvailableIntangibleSeatCount(companyId, item.getId())
			));
		return AssetRequestAssignableItemsResponse.builder()
			.requestedItem(requestedItem)
			.items(PaginationResponse.from(items))
			.build();
	}

	@Transactional
	public AssetRequestAssignResponse assignAssetRequest(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		AssetRequestAssignRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member assignee = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		AssetRequestTicket assetRequestTicket = findAssetRequestTicket(ticketId, companyId);
		Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

		validateAssignable(ticket, assignee);
		validateAssignmentTarget(assetRequestTicket, request);

		if (request.getAssetType() == AssetType.TANGIBLE) {
			TangibleAssetItem item = findTangibleAssetItem(request.getItemId(), companyId);
			List<AssetRequestAssignResponse.AssignedAssetSummary> assignedAssets = assignTangibleAssets(
				ticket,
				item,
				assetRequestTicket.getQuantity(),
				companyId
			);
			assetRequestTicket.markAssigned();
			ticket.changeProcessingStatus(TicketStatus.IN_PROGRESS, java.time.LocalDateTime.now());
			return AssetRequestAssignResponse.from(
				ticket,
				assetRequestTicket,
				AssetType.TANGIBLE,
				item.getId(),
				item.getProductName(),
				assignedAssets
			);
		}

		IntangibleAssetItem item = findIntangibleAssetItem(request.getItemId(), companyId);
		List<AssetRequestAssignResponse.AssignedAssetSummary> assignedAssets = assignIntangibleAssets(
			ticket,
			item,
			assetRequestTicket.getQuantity(),
			companyId
		);
		assetRequestTicket.markAssigned();
		ticket.changeProcessingStatus(TicketStatus.IN_PROGRESS, java.time.LocalDateTime.now());
		return AssetRequestAssignResponse.from(
			ticket,
			assetRequestTicket,
			AssetType.INTANGIBLE,
			item.getId(),
			item.getProductName(),
			assignedAssets
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

	private AssetRequestTicket findAssetRequestTicket(UUID ticketId, UUID companyId) {
		return assetRequestTicketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
	}

	private AssetType resolveSearchAssetType(
		AssetRequestTicket assetRequestTicket,
		AssetRequestAssignableItemSearchRequest request
	) {
		if (request.getAssetType() != null) {
			return request.getAssetType();
		}
		if (assetRequestTicket.getTangibleAssetItem() != null) {
			return AssetType.TANGIBLE;
		}
		return AssetType.INTANGIBLE;
	}

	private AssetRequestAssignableItemResponse getRequestedItem(
		AssetRequestTicket assetRequestTicket,
		AssetType assetType
	) {
		if (assetType == AssetType.TANGIBLE) {
			TangibleAssetItem item = assetRequestTicket.getTangibleAssetItem();
			if (item == null) {
				return null;
			}
			return AssetRequestAssignableItemResponse.from(
				item,
				item.getId(),
				getAvailableTangibleCount(assetRequestTicket.getCompany().getId(), item.getId())
			);
		}

		IntangibleAssetItem item = assetRequestTicket.getIntangibleAssetItem();
		if (item == null) {
			return null;
		}
		return AssetRequestAssignableItemResponse.from(
			item,
			item.getId(),
			getAvailableIntangibleSeatCount(assetRequestTicket.getCompany().getId(), item.getId())
		);
	}

	private int getAvailableTangibleCount(UUID companyId, UUID itemId) {
		return Math.toIntExact(tangibleAssetRepository.countByCompany_IdAndTangibleAssetItem_IdAndTangibleAssetStatus(
			companyId,
			itemId,
			TangibleAssetStatus.AVAILABLE
		));
	}

	private int getAvailableIntangibleSeatCount(UUID companyId, UUID itemId) {
		List<IntangibleAsset> assets = intangibleAssetRepository.findAllByCompany_IdAndIntangibleAssetItem_IdAndIntangibleAssetStatusIn(
			companyId,
			itemId,
			List.of(IntangibleAssetStatus.AVAILABLE, IntangibleAssetStatus.IN_USE)
		);
		int availableSeatCount = 0;
		for (IntangibleAsset asset : assets) {
			long activeAssignmentCount = intangibleAssetAssignmentRepository
				.countByCompany_IdAndIntangibleAsset_IdAndAssignmentStatus(
					companyId,
					asset.getId(),
					AssignmentStatus.ACTIVE
				);
			availableSeatCount += Math.max(asset.getSeatCount() - Math.toIntExact(activeAssignmentCount), 0);
		}
		return availableSeatCount;
	}

	private void validateAssignable(Ticket ticket, Member member) {
		if (!isAssetRole(member.getRole())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		if (ticket.getTicketStatus() != TicketStatus.ASSET_APPROVED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매자산팀 승인 상태의 자산요청만 할당할 수 있습니다.");
		}
		if (ticket.getRequester().getId().equals(member.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateAssignmentTarget(
		AssetRequestTicket assetRequestTicket,
		AssetRequestAssignRequest request
	) {
		if (assetRequestTicket.getStatus() != AssetRequestTicketStatus.REQUESTED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 할당 처리된 자산요청입니다.");
		}
		boolean tangibleRequest = assetRequestTicket.getTangibleAssetItem() != null;
		if (tangibleRequest && request.getAssetType() != AssetType.TANGIBLE) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형자산 요청에는 유형자산 품목만 할당할 수 있습니다.");
		}
		if (!tangibleRequest && request.getAssetType() != AssetType.INTANGIBLE) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산 요청에는 무형자산 품목만 할당할 수 있습니다.");
		}
	}

	private List<AssetRequestAssignResponse.AssignedAssetSummary> assignTangibleAssets(
		Ticket ticket,
		TangibleAssetItem item,
		int quantity,
		UUID companyId
	) {
		List<TangibleAsset> assets = tangibleAssetRepository.findAvailableAssetsWithLock(
			companyId,
			item.getId(),
			TangibleAssetStatus.AVAILABLE,
			PageRequest.of(0, quantity)
		);
		if (assets.size() < quantity) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "할당 가능한 유형자산 재고가 부족합니다.");
		}

		List<AssetRequestAssignResponse.AssignedAssetSummary> assignedAssets = new ArrayList<>();
		for (TangibleAsset asset : assets) {
			TangibleAssetAssignment assignment = TangibleAssetAssignment.builder()
				.company(ticket.getCompany())
				.tangibleAsset(asset)
				.member(ticket.getRequester())
				.department(ticket.getDepartment())
				.assignmentType(UsageType.PERMANENT)
				.assignmentStatus(com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus.ACTIVE)
				.build();
			tangibleAssetAssignmentRepository.save(assignment);
			asset.markInUse(
				ticket.getRequester(),
				ticket.getDepartment(),
				UsageType.PERMANENT,
				resolveAssetUsageType(assetRequestUsageType(ticket)),
				assignment.getAssignedAt(),
				null
			);
			assignedAssets.add(AssetRequestAssignResponse.AssignedAssetSummary.builder()
				.assetId(asset.getId())
				.assetCode(asset.getAssetCode())
				.build());
		}
		return assignedAssets;
	}

	private List<AssetRequestAssignResponse.AssignedAssetSummary> assignIntangibleAssets(
		Ticket ticket,
		IntangibleAssetItem item,
		int quantity,
		UUID companyId
	) {
		List<IntangibleAsset> candidates = intangibleAssetRepository.findAssignableAssetsWithLock(
			companyId,
			item.getId(),
			List.of(IntangibleAssetStatus.AVAILABLE, IntangibleAssetStatus.IN_USE),
			PageRequest.of(0, Math.max(quantity * 5, 10))
		);
		List<AssetRequestAssignResponse.AssignedAssetSummary> assignedAssets = new ArrayList<>();
		for (IntangibleAsset asset : candidates) {
			while (assignedAssets.size() < quantity && hasAvailableSeat(asset, companyId)) {
				if (intangibleAssetAssignmentRepository.existsByCompany_IdAndIntangibleAsset_IdAndMember_IdAndAssignmentStatus(
					companyId,
					asset.getId(),
					ticket.getRequester().getId(),
					AssignmentStatus.ACTIVE
				)) {
					break;
				}
				IntangibleAssetAssignment assignment = IntangibleAssetAssignment.builder()
					.company(ticket.getCompany())
					.intangibleAsset(asset)
					.member(ticket.getRequester())
					.department(ticket.getDepartment())
					.assignmentStatus(AssignmentStatus.ACTIVE)
					.build();
				intangibleAssetAssignmentRepository.save(assignment);
				if (asset.getSeatCount() == 1) {
					asset.assignTo(ticket.getRequester(), ticket.getDepartment());
				} else {
					asset.markInUse();
				}
				assignedAssets.add(AssetRequestAssignResponse.AssignedAssetSummary.builder()
					.assetId(asset.getId())
					.assetCode(asset.getAssetCode())
					.build());
			}
			if (assignedAssets.size() == quantity) {
				return assignedAssets;
			}
		}
		throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "할당 가능한 무형자산 좌석이 부족합니다.");
	}

	private boolean hasAvailableSeat(IntangibleAsset asset, UUID companyId) {
		long activeAssignmentCount = intangibleAssetAssignmentRepository
			.countByCompany_IdAndIntangibleAsset_IdAndAssignmentStatus(
				companyId,
				asset.getId(),
				AssignmentStatus.ACTIVE
			);
		return activeAssignmentCount < asset.getSeatCount();
	}

	private com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType assetRequestUsageType(Ticket ticket) {
		AssetRequestTicket assetRequestTicket = assetRequestTicketRepository
			.findByIdAndCompany_IdAndDeletedAtIsNull(ticket.getId(), ticket.getCompany().getId())
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		return assetRequestTicket.getRequestedUsageType();
	}

	private AssetUsageType resolveAssetUsageType(
		com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType requestedUsageType
	) {
		return switch (requestedUsageType) {
			case PERSONAL -> AssetUsageType.PERSONAL;
			case DEPARTMENT -> AssetUsageType.DEPARTMENT;
		};
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
		return status == TicketStatus.ASSET_APPROVED || status == TicketStatus.IN_PROGRESS;
	}

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
