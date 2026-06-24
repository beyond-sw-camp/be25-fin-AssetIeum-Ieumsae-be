package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.assetrequest.repository.AssetRequestTicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.dto.PurchasePlanCandidateTicketResponse;
import com.ieumsae.assetieum.domain.ticket.common.dto.PurchasePlanCandidateTicketSearchRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.PurchaseRequestTicketRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchasePlanCandidateTicketService {

    private final CompanyRepository companyRepository;
    private final TangibleAssetRepository tangibleAssetRepository;
    private final IntangibleAssetRepository intangibleAssetRepository;
    private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
    private final AssetRequestTicketRepository assetRequestTicketRepository;
    private final PurchaseRequestTicketRepository purchaseRequestTicketRepository;

    public PaginationResponse<PurchasePlanCandidateTicketResponse> getPurchasePlanCandidateTickets(
            PurchasePlanCandidateTicketSearchRequest request,
            UUID companyId
    ) {
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        List<PurchasePlanCandidateTicketResponse> candidates = new ArrayList<>();
        candidates.addAll(assetRequestTicketRepository.findPurchasePlanCandidates(companyId)
                .stream()
                .filter(ticket -> !hasEnoughInventory(ticket, companyId))
                .map(ticket -> PurchasePlanCandidateTicketResponse.from(
                        ticket,
                        resolveRecentPurchasePrice(ticket, companyId)
                ))
                .toList());
        candidates.addAll(purchaseRequestTicketRepository.findPurchasePlanCandidates(companyId)
                .stream()
                .map(PurchasePlanCandidateTicketResponse::from)
                .toList());

        candidates.sort(Comparator.comparing(
                PurchasePlanCandidateTicketResponse::getRequestedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        int start = Math.toIntExact(Math.min(request.toPageable().getOffset(), candidates.size()));
        int end = Math.min(start + request.toPageable().getPageSize(), candidates.size());
        Page<PurchasePlanCandidateTicketResponse> page = new PageImpl<>(
                candidates.subList(start, end),
                request.toPageable(),
                candidates.size()
        );

        return PaginationResponse.from(page);
    }

    private BigDecimal resolveRecentPurchasePrice(AssetRequestTicket ticket, UUID companyId) {
        if (ticket.getTangibleAssetItem() != null) {
            return tangibleAssetRepository.findRecentPurchasePrices(
                            companyId,
                            ticket.getTangibleAssetItem().getId(),
                            PageRequest.of(0, 1)
                    )
                    .stream()
                    .findFirst()
                    .orElse(null);
        }

        return intangibleAssetRepository.findRecentPurchasePrices(
                        companyId,
                        ticket.getIntangibleAssetItem().getId(),
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    private boolean hasEnoughInventory(AssetRequestTicket ticket, UUID companyId) {
        if (ticket.getTangibleAssetItem() != null) {
            long availableCount = tangibleAssetRepository.countByCompany_IdAndTangibleAssetItem_IdAndTangibleAssetStatus(
                    companyId,
                    ticket.getTangibleAssetItem().getId(),
                    TangibleAssetStatus.AVAILABLE
            );
            return availableCount >= ticket.getQuantity();
        }

        return getAvailableIntangibleSeatCount(
                companyId,
                ticket.getIntangibleAssetItem().getId(),
                ticket.getTicket().getDepartment().getId()
        ) >= ticket.getQuantity();
    }

    private int getAvailableIntangibleSeatCount(UUID companyId, UUID itemId, UUID requesterDepartmentId) {
        List<IntangibleAsset> assets = intangibleAssetRepository.findAllByCompany_IdAndIntangibleAssetItem_IdAndIntangibleAssetStatusIn(
                companyId,
                itemId,
                List.of(IntangibleAssetStatus.AVAILABLE, IntangibleAssetStatus.IN_USE)
        );

        int availableSeatCount = 0;
        for (IntangibleAsset asset : assets) {
            if (asset.getDepartment() != null
                    && !asset.getDepartment().getId().equals(requesterDepartmentId)) {
                continue;
            }
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
}
