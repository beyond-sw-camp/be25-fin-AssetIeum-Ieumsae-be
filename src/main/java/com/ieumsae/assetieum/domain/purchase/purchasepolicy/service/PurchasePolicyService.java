package com.ieumsae.assetieum.domain.purchase.purchasepolicy.service;

import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.purchase.purchasepolicy.dto.PurchasePolicyRequest;
import com.ieumsae.assetieum.domain.purchase.purchasepolicy.dto.PurchasePolicyResponse;
import com.ieumsae.assetieum.domain.purchase.purchasepolicy.entity.PurchasePolicy;
import com.ieumsae.assetieum.domain.purchase.purchasepolicy.repository.PurchasePolicyRepository;
import com.ieumsae.assetieum.domain.purchase.purchasepolicy.type.PurchaseMethod;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.PurchaseRequestTicketRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchasePolicyService {

    private static final List<TicketStatus> IN_PROGRESS_TICKET_STATUSES = List.of(
            TicketStatus.REQUESTED,
            TicketStatus.DEPARTMENT_APPROVED,
            TicketStatus.ASSET_APPROVED,
            TicketStatus.IN_PROGRESS
    );

    private final CompanyRepository companyRepository;
    private final PurchasePolicyRepository purchasePolicyRepository;
    private final PurchaseRequestTicketRepository purchaseRequestTicketRepository;

    @Transactional
    public PurchasePolicyResponse updatePurchasePolicy(
            PurchasePolicyRequest request,
            UUID companyId
    ) {
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        PurchasePolicy purchasePolicy = purchasePolicyRepository.findByCompany_Id(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_POLICY_NOT_FOUND));

        validatePurchaseMethodChange(companyId, request.getPurchaseMethod());

        purchasePolicy.update(request.getPurchaseMethod(), request.getOverPercentageLimit());

        return PurchasePolicyResponse.from(purchasePolicy);
    }

    private void validatePurchaseMethodChange(UUID companyId, PurchaseMethod purchaseMethod) {
        if (purchaseMethod == null || purchaseMethod == PurchaseMethod.PARALLEL) {
            return;
        }

        if (purchaseMethod == PurchaseMethod.ONLY_DIRECT_PURCHASE
                && hasInProgressPurchaseRequestTicket(companyId, RequestMethod.TEAM_PURCHASE)) {
            throw new BusinessException(ErrorCode.PURCHASE_POLICY_TEAM_PURCHASE_TICKET_IN_PROGRESS);
        }

        if (purchaseMethod == PurchaseMethod.ONLY_ASSET_TEAM
                && hasInProgressPurchaseRequestTicket(companyId, RequestMethod.DIRECT_PURCHASE)) {
            throw new BusinessException(ErrorCode.PURCHASE_POLICY_DIRECT_PURCHASE_TICKET_IN_PROGRESS);
        }
    }

    private boolean hasInProgressPurchaseRequestTicket(UUID companyId, RequestMethod requestMethod) {
        return purchaseRequestTicketRepository
                .existsByCompany_IdAndRequestMethodAndDeletedAtIsNullAndTicket_TicketStatusIn(
                        companyId,
                        requestMethod,
                        IN_PROGRESS_TICKET_STATUSES
                );
    }
}
