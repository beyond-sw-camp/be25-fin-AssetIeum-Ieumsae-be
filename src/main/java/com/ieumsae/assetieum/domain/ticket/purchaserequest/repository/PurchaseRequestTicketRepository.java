package com.ieumsae.assetieum.domain.ticket.purchaserequest.repository;

import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseRequestTicketRepository extends JpaRepository<PurchaseRequestTicket, UUID> {

    Optional<PurchaseRequestTicket> findByIdAndCompany_Id(UUID ticketId, UUID companyId);

    boolean existsByCompany_IdAndRequestMethodAndDeletedAtIsNullAndTicket_TicketStatusIn(
            UUID companyId,
            RequestMethod requestMethod,
            Collection<TicketStatus> ticketStatuses
    );

    @Query("""
        select prt
        from PurchaseRequestTicket prt
        join fetch prt.ticket t
        join fetch t.requester
        left join fetch prt.tangibleAssetItem tai
        left join fetch tai.tangibleAssetCategory
        left join fetch prt.intangibleAssetItem iai
        left join fetch iai.intangibleAssetCategory
        left join fetch prt.tangibleAssetCategory
        left join fetch prt.intangibleAssetCategory
        where prt.company.id = :companyId
            and prt.deletedAt is null
            and prt.requestMethod = com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod.TEAM_PURCHASE
            and prt.status = com.ieumsae.assetieum.domain.ticket.purchaserequest.type.PurchaseRequestTicketStatus.REQUESTED
            and t.ticketStatus = com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus.ASSET_APPROVED
            and not exists (
                select 1
                from PurchasePlanItem ppi
                where ppi.ticket = t
            )
        """)
    List<PurchaseRequestTicket> findPurchasePlanCandidates(@Param("companyId") UUID companyId);
}
