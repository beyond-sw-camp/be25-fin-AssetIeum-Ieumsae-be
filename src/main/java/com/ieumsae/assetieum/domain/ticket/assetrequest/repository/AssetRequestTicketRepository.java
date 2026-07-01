package com.ieumsae.assetieum.domain.ticket.assetrequest.repository;

import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRequestTicketRepository extends JpaRepository<AssetRequestTicket, UUID> {

	Optional<AssetRequestTicket> findByIdAndCompany_IdAndDeletedAtIsNull(UUID ticketId, UUID companyId);

	@Query("""
		select art
		from AssetRequestTicket art
		join fetch art.ticket t
		join fetch t.requester
		left join fetch art.tangibleAssetItem tai
		left join fetch tai.tangibleAssetCategory
		left join fetch art.intangibleAssetItem iai
		left join fetch iai.intangibleAssetCategory
		where art.company.id = :companyId
			and art.deletedAt is null
			and art.status = com.ieumsae.assetieum.domain.ticket.assetrequest.type.AssetRequestTicketStatus.REQUESTED
			and t.ticketStatus = com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus.ASSET_APPROVED
			and not exists (
				select 1
				from PurchasePlanItem ppi
				join ppi.purchasePlan pp
				where ppi.ticket = t
					and pp.deletedAt is null
					and pp.purchaseRequestStatus not in (
						com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus.REJECTED,
						com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus.CANCELLED
					)
			)
		""")
	List<AssetRequestTicket> findPurchasePlanCandidates(@Param("companyId") UUID companyId);

	List<AssetRequestTicket> findAllByTicket_RequestReasonAndTicket_TicketStatusAndTicket_CreatedAtGreaterThanEqualAndTicket_CreatedAtLessThanAndDeletedAtIsNullOrderByTicket_CreatedAtAsc(
		String requestReason,
		TicketStatus ticketStatus,
		LocalDateTime startInclusive,
		LocalDateTime endExclusive
	);
}
