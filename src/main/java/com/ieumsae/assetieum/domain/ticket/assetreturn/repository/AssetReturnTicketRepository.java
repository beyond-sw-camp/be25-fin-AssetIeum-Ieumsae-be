package com.ieumsae.assetieum.domain.ticket.assetreturn.repository;

import com.ieumsae.assetieum.domain.ticket.assetreturn.entity.AssetReturnTicket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetReturnTicketRepository extends JpaRepository<AssetReturnTicket, UUID> {

	boolean existsByCompany_IdAndTangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
		UUID companyId,
		UUID tangibleAssetId,
		Collection<TicketStatus> ticketStatuses
	);

	boolean existsByCompany_IdAndIntangibleAsset_IdAndTicket_TicketStatusInAndDeletedAtIsNull(
		UUID companyId,
		UUID intangibleAssetId,
		Collection<TicketStatus> ticketStatuses
	);

	List<AssetReturnTicket> findAllByTicket_RequestReasonAndTicket_TicketStatusAndTicket_CreatedAtGreaterThanEqualAndTicket_CreatedAtLessThanAndDeletedAtIsNullOrderByTicket_CreatedAtAsc(
		String requestReason,
		TicketStatus ticketStatus,
		LocalDateTime startInclusive,
		LocalDateTime endExclusive
	);

	Optional<AssetReturnTicket> findByIdAndCompany_IdAndDeletedAtIsNull(UUID ticketId, UUID companyId);
}
