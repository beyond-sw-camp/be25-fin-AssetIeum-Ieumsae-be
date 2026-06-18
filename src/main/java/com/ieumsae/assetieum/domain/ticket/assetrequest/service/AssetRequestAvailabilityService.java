package com.ieumsae.assetieum.domain.ticket.assetrequest.service;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignableItemResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignableItemSearchRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignableItemsResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetRequestAvailabilityService {

	private final TangibleAssetItemRepository tangibleAssetItemRepository;
	private final IntangibleAssetItemRepository intangibleAssetItemRepository;
	private final TangibleAssetRepository tangibleAssetRepository;
	private final IntangibleAssetRepository intangibleAssetRepository;
	private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;

	public AssetRequestAssignableItemsResponse getAssignableItems(
		UUID companyId,
		AssetRequestTicket assetRequestTicket,
		AssetRequestAssignableItemSearchRequest request
	) {
		AssetType assetType = resolveSearchAssetType(assetRequestTicket, request);
		AssetRequestAssignableItemResponse requestedItem = getRequestedItem(assetRequestTicket, assetType);
		UUID requestedItemId = requestedItem == null ? null : requestedItem.getItemId();

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
					requestedItemId,
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
				requestedItemId,
				getAvailableIntangibleSeatCount(companyId, item.getId())
			));
		return AssetRequestAssignableItemsResponse.builder()
			.requestedItem(requestedItem)
			.items(PaginationResponse.from(items))
			.build();
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

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
