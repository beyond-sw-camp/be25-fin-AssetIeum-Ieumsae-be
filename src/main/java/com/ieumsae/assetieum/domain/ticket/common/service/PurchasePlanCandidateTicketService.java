package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
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
}
