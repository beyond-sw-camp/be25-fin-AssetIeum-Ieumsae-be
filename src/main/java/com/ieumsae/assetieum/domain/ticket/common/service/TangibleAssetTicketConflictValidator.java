package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.ticket.assetreturn.repository.AssetReturnTicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.maintenance.repository.MaintenanceTicketRepository;
import com.ieumsae.assetieum.domain.ticket.purchasereturn.repository.PurchaseReturnTicketRepository;
import com.ieumsae.assetieum.domain.ticket.rental.repository.RentalTicketRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TangibleAssetTicketConflictValidator {

	private static final List<TicketStatus> ONGOING_STATUSES = List.of(
		TicketStatus.REQUESTED,
		TicketStatus.DEPARTMENT_APPROVED,
		TicketStatus.IN_PROGRESS
	);

	private final RentalTicketRepository rentalTicketRepository;
	private final MaintenanceTicketRepository maintenanceTicketRepository;
	private final AssetReturnTicketRepository assetReturnTicketRepository;
	private final PurchaseReturnTicketRepository purchaseReturnTicketRepository;
	private final TangibleAssetRepository tangibleAssetRepository;

	public void validateNoOngoingTangibleAssetTicket(UUID companyId, UUID tangibleAssetId) {
		tangibleAssetRepository.findWithLockByIdAndCompany_Id(tangibleAssetId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));

		if (hasOngoingRentalExtension(companyId, tangibleAssetId)
			|| hasOngoingMaintenance(companyId, tangibleAssetId)
			|| hasOngoingAssetReturn(companyId, tangibleAssetId)
			|| hasOngoingPurchaseReturn(companyId, tangibleAssetId)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 진행 중인 자산 요청 티켓이 있습니다.");
		}
	}

	private boolean hasOngoingRentalExtension(UUID companyId, UUID tangibleAssetId) {
		return rentalTicketRepository
			.existsByCompany_IdAndTangibleAsset_IdAndTicket_TicketTypeAndTicket_TicketStatusInAndDeletedAtIsNull(
				companyId,
				tangibleAssetId,
				TicketType.RENTAL_EXTENSION,
				ONGOING_STATUSES
			);
	}

	private boolean hasOngoingMaintenance(UUID companyId, UUID tangibleAssetId) {
		return maintenanceTicketRepository
			.existsByCompany_IdAndTangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
				companyId,
				tangibleAssetId,
				ONGOING_STATUSES
			);
	}

	private boolean hasOngoingAssetReturn(UUID companyId, UUID tangibleAssetId) {
		return assetReturnTicketRepository
			.existsByCompany_IdAndTangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
				companyId,
				tangibleAssetId,
				ONGOING_STATUSES
			);
	}

	private boolean hasOngoingPurchaseReturn(UUID companyId, UUID tangibleAssetId) {
		return purchaseReturnTicketRepository
			.existsByCompany_IdAndTangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
				companyId,
				tangibleAssetId,
				ONGOING_STATUSES
			);
	}
}
