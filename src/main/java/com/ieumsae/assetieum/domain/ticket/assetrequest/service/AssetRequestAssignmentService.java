package com.ieumsae.assetieum.domain.ticket.assetrequest.service;

import com.ieumsae.assetieum.domain.budget.budget.service.BudgetExecutionService;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.AssetUsageType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetRequestAssignmentService {

	private final TicketRepository ticketRepository;
	private final TangibleAssetItemRepository tangibleAssetItemRepository;
	private final IntangibleAssetItemRepository intangibleAssetItemRepository;
	private final TangibleAssetRepository tangibleAssetRepository;
	private final IntangibleAssetRepository intangibleAssetRepository;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
	private final AssetRequestValidator assetRequestValidator;
	private final BudgetExecutionService budgetExecutionService;

	@Transactional
	public AssetRequestAssignResponse assign(
		UUID companyId,
		Member assignee,
		AssetRequestTicket assetRequestTicket,
		AssetRequestAssignRequest request
	) {
		Ticket ticket = ticketRepository
			.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(assetRequestTicket.getId(), companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

		assetRequestValidator.validateAssignable(ticket, assignee);
		assetRequestValidator.validateAssignmentTarget(assetRequestTicket, request);

		if (request.getAssetType() == AssetType.TANGIBLE) {
			TangibleAssetItem item = findTangibleAssetItem(request.getItemId(), companyId);
			List<AssetRequestAssignResponse.AssignedAssetSummary> assignedAssets = assignTangibleAssets(
				ticket,
				item,
				assetRequestTicket.getQuantity(),
				assetRequestTicket.getRequestedUsageType(),
				companyId
			);
			budgetExecutionService.releaseHoldForInventoryAssignment(ticket, companyId);
			assetRequestTicket.complete();
			ticket.changeProcessingStatus(TicketStatus.COMPLETED, LocalDateTime.now());
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
		budgetExecutionService.releaseHoldForInventoryAssignment(ticket, companyId);
		assetRequestTicket.complete();
		ticket.changeProcessingStatus(TicketStatus.COMPLETED, LocalDateTime.now());
		return AssetRequestAssignResponse.from(
			ticket,
			assetRequestTicket,
			AssetType.INTANGIBLE,
			item.getId(),
			item.getProductName(),
			assignedAssets
		);
	}

	private List<AssetRequestAssignResponse.AssignedAssetSummary> assignTangibleAssets(
		Ticket ticket,
		TangibleAssetItem item,
		int quantity,
		RequestedUsageType requestedUsageType,
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
				resolveAssetUsageType(requestedUsageType),
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

	private AssetUsageType resolveAssetUsageType(RequestedUsageType requestedUsageType) {
		return switch (requestedUsageType) {
			case PERSONAL -> AssetUsageType.PERSONAL;
			case DEPARTMENT -> AssetUsageType.DEPARTMENT;
		};
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
}
