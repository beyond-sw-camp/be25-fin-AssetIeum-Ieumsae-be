package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.ticket.assetreturn.repository.AssetReturnTicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.repository.PurchaseReturnTicketRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntangibleAssetTicketConflictValidator {

	private static final List<TicketStatus> ONGOING_STATUSES = List.of(
		TicketStatus.REQUESTED,
		TicketStatus.DEPARTMENT_APPROVED,
		TicketStatus.IN_PROGRESS
	);

	private final AssetReturnTicketRepository assetReturnTicketRepository;
	private final PurchaseReturnTicketRepository purchaseReturnTicketRepository;
	private final IntangibleAssetRepository intangibleAssetRepository;

	public void validateNoOngoingIntangibleAssetReturnTicket(UUID companyId, UUID intangibleAssetId) {
		intangibleAssetRepository.findWithLockByIdAndCompany_Id(intangibleAssetId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_FOUND));

		boolean existsAssetReturn = assetReturnTicketRepository
			.existsByCompany_IdAndIntangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
				companyId,
				intangibleAssetId,
				ONGOING_STATUSES
			);
		boolean existsPurchaseReturn = purchaseReturnTicketRepository
			.existsByCompany_IdAndIntangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
				companyId,
				intangibleAssetId,
				ONGOING_STATUSES
			);

		if (existsAssetReturn || existsPurchaseReturn) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 진행 중인 무형자산 반납/반품 요청이 있습니다.");
		}
	}
}
