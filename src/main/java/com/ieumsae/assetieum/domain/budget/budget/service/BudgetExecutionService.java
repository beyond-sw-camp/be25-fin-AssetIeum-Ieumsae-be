package com.ieumsae.assetieum.domain.budget.budget.service;

import com.ieumsae.assetieum.domain.budget.budget.entity.Budget;
import com.ieumsae.assetieum.domain.budget.budget.repository.BudgetRepository;
import com.ieumsae.assetieum.domain.budget.history.entity.BudgetHistory;
import com.ieumsae.assetieum.domain.budget.history.repository.BudgetHistoryRepository;
import com.ieumsae.assetieum.domain.budget.history.type.BudgetHistoryType;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlan;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.assetrequest.repository.AssetRequestTicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.PurchaseRequestTicketRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BudgetExecutionService {

    private final BudgetRepository budgetRepository;
    private final BudgetHistoryRepository budgetHistoryRepository;
    private final AssetRequestTicketRepository assetRequestTicketRepository;
    private final TangibleAssetRepository tangibleAssetRepository;
    private final IntangibleAssetRepository intangibleAssetRepository;
    private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
    private final PurchaseRequestTicketRepository purchaseRequestTicketRepository;

    @Transactional
    public void holdForAssetRequest(Ticket ticket, UUID companyId) {
        if (ticket.getTicketType() != TicketType.ASSET_REQUEST) {
            return;
        }

        // 부서 승인 시점에는 아직 구매 여부가 확정되지 않았으므로 예상 금액만 선점한다.
        AssetRequestTicket assetRequestTicket = findAssetRequestTicket(ticket.getId(), companyId);
        if (hasEnoughInventory(assetRequestTicket, companyId)) {
            return;
        }

        BigDecimal amount = resolveEstimatedAmount(assetRequestTicket, companyId);
        Budget budget = findBudgetForAssetRequest(ticket, assetRequestTicket);
        validateAvailableBudget(budget, amount);

        BigDecimal holdBefore = budget.getHeldAmount();
        BigDecimal usedBefore = budget.getUsedAmount();
        budget.increaseHold(amount);

        saveHistory(
                budget,
                ticket,
                null,
                BudgetHistoryType.HOLD_INCREASE,
                amount,
                usedBefore,
                budget.getUsedAmount(),
                holdBefore,
                budget.getHeldAmount(),
                "Asset request department approval budget hold"
        );
    }

    @Transactional
    public void holdForPurchaseRequest(Ticket ticket, UUID companyId) {
        if (ticket.getTicketType() != TicketType.PURCHASE_REQUEST) {
            return;
        }

        PurchaseRequestTicket purchaseRequestTicket = findPurchaseRequestTicket(ticket.getId(), companyId);
        BigDecimal amount = resolvePurchaseRequestEstimatedAmount(purchaseRequestTicket);
        Budget budget = findBudgetForPurchaseRequest(ticket, purchaseRequestTicket);
        validateAvailableBudget(budget, amount);

        BigDecimal holdBefore = budget.getHeldAmount();
        BigDecimal usedBefore = budget.getUsedAmount();
        budget.increaseHold(amount);

        saveHistory(
                budget,
                ticket,
                null,
                BudgetHistoryType.HOLD_INCREASE,
                amount,
                usedBefore,
                budget.getUsedAmount(),
                holdBefore,
                budget.getHeldAmount(),
                "Purchase request department approval budget hold"
        );
    }

    @Transactional
    public void releaseHoldForInventoryAssignment(Ticket ticket, UUID companyId) {
        if (ticket.getTicketType() != TicketType.ASSET_REQUEST) {
            return;
        }

        // 보유 재고를 바로 할당한 경우 실제 지출이 없으므로 선점 예산만 해제한다.
        BigDecimal amount = getOutstandingHoldAmount(ticket, companyId);
        if (amount.signum() <= 0) {
            return;
        }

        Budget budget = findBudgetForTicket(ticket, companyId);
        decreaseHold(budget, ticket, null, amount, "Release budget hold after inventory assignment");
    }

    @Transactional
    public void releaseHoldForCancellation(Ticket ticket, UUID companyId) {
        if (ticket.getTicketType() != TicketType.ASSET_REQUEST
                && ticket.getTicketType() != TicketType.PURCHASE_REQUEST) {
            return;
        }

        // 취소/반려된 자산요청은 더 이상 예산을 잡아둘 필요가 없으므로 남은 선점액을 해제한다.
        BigDecimal amount = getOutstandingHoldAmount(ticket, companyId);
        if (amount.signum() <= 0) {
            return;
        }

        Budget budget = findBudgetForTicket(ticket, companyId);
        decreaseHold(budget, ticket, null, amount, "Release budget hold after ticket cancellation");
    }

    @Transactional
    public void releaseHoldForPurchasePlanCancellation(
            PurchasePlan purchasePlan,
            List<PurchasePlanItem> items,
            UUID companyId
    ) {
        // 구매계획 반려/취소 시 연결된 자산요청별 남은 선점액만 해제한다.
        for (Ticket ticket : getAssetRequestTickets(items, companyId)) {
            BigDecimal amount = getOutstandingHoldAmount(ticket, companyId);
            if (amount.signum() <= 0) {
                continue;
            }

            Budget budget = findBudgetForTicket(ticket, companyId);
            decreaseHold(budget, ticket, purchasePlan, amount, "Release budget hold after purchase plan cancellation");
        }
    }

    @Transactional
    public void holdForPurchasePlanCreation(
            List<PurchasePlanItem> items,
            UUID companyId
    ) {
        Set<UUID> heldTicketIds = new HashSet<>();
        for (Ticket ticket : getAssetRequestTickets(items, companyId)) {
            if (!heldTicketIds.add(ticket.getId()) || getOutstandingHoldAmount(ticket, companyId).signum() > 0) {
                continue;
            }
            holdForAssetRequest(ticket, companyId);
            holdForPurchaseRequest(ticket, companyId);
        }
    }

    @Transactional
    public void executeForPurchasePlanCompletion(
            PurchasePlan purchasePlan,
            List<PurchasePlanItem> items,
            UUID companyId
    ) {
        // 현재 구매계획에는 실제 결제 금액이 없으므로 자산요청의 남은 선점액을 기준으로 집행 처리한다.
        for (Ticket ticket : getAssetRequestTickets(items, companyId)) {
            if (hasPendingLinkedPurchasePlanItem(items, ticket)) {
                continue;
            }

            BigDecimal amount = getOutstandingHoldAmount(ticket, companyId);
            if (amount.signum() <= 0) {
                continue;
            }

            Budget budget = findBudgetForTicket(ticket, companyId);
            decreaseHold(budget, ticket, purchasePlan, amount, "Release budget hold before purchase plan execution");
            increaseUsed(budget, ticket, purchasePlan, amount, "Execute budget after purchase plan completion");
        }
    }

    @Transactional
    public void executeForPurchasePlanItemRegistration(
            PurchasePlan purchasePlan,
            PurchasePlanItem item,
            UUID companyId,
            BigDecimal actualAmount
    ) {
        Ticket ticket = item.getTicket();
        if (ticket == null
                || (ticket.getTicketType() != TicketType.ASSET_REQUEST
                && ticket.getTicketType() != TicketType.PURCHASE_REQUEST)) {
            return;
        }

        BigDecimal holdAmount = getOutstandingHoldAmount(ticket, companyId);
        if (holdAmount.signum() <= 0) {
            return;
        }

        Budget budget = findBudgetForTicket(ticket, companyId);
        settleActualExecution(
                budget,
                ticket,
                purchasePlan,
                holdAmount,
                actualAmount,
                "Release budget hold before actual purchase execution",
                "Execute budget with actual purchase amount"
        );
    }

    @Transactional
    public void executeForDirectPurchaseResult(
            Ticket ticket,
            UUID companyId,
            BigDecimal actualAmount
    ) {
        if (ticket.getTicketType() != TicketType.PURCHASE_REQUEST) {
            return;
        }

        BigDecimal holdAmount = getOutstandingHoldAmount(ticket, companyId);
        if (holdAmount.signum() <= 0) {
            return;
        }

        Budget budget = findBudgetForTicket(ticket, companyId);
        settleActualExecution(
                budget,
                ticket,
                null,
                holdAmount,
                actualAmount,
                "Release budget hold before direct purchase execution",
                "Execute budget with direct purchase actual amount"
        );
    }

    @Transactional
    public void executeForMaintenanceCompletion(
            Ticket ticket,
            UUID companyId,
            BigDecimal maintenanceCost
    ) {
        if (ticket.getTicketType() != TicketType.MAINTENANCE_REQUEST
                || maintenanceCost == null
                || maintenanceCost.signum() <= 0) {
            return;
        }

        Budget budget = findCompanyCommonBudget(ticket);
        validateAvailableBudget(budget, maintenanceCost);
        increaseUsed(budget, ticket, null, maintenanceCost, "Execute company common budget for maintenance completion");
    }

    private List<Ticket> getAssetRequestTickets(List<PurchasePlanItem> items, UUID companyId) {
        Set<UUID> ticketIds = new HashSet<>();
        return items.stream()
                .map(PurchasePlanItem::getTicket)
                .filter(ticket -> ticket != null
                        && (ticket.getTicketType() == TicketType.ASSET_REQUEST
                        || ticket.getTicketType() == TicketType.PURCHASE_REQUEST))
                .filter(ticket -> ticketIds.add(ticket.getId()))
                .toList();
    }

    private boolean hasPendingLinkedPurchasePlanItem(List<PurchasePlanItem> items, Ticket ticket) {
        return items.stream()
                .filter(item -> item.getTicket() != null)
                .filter(item -> item.getTicket().getId().equals(ticket.getId()))
                .anyMatch(item -> item.getPurchasePlanItemStatus()
                        != com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchasePlanItemStatus.ASSET_REGISTERED);
    }

    private Budget findBudgetForTicket(Ticket ticket, UUID companyId) {
        if (ticket.getTicketType() == TicketType.PURCHASE_REQUEST) {
            PurchaseRequestTicket purchaseRequestTicket = findPurchaseRequestTicket(ticket.getId(), companyId);
            return findBudgetForPurchaseRequest(ticket, purchaseRequestTicket);
        }

        AssetRequestTicket assetRequestTicket = findAssetRequestTicket(ticket.getId(), companyId);
        return findBudgetForAssetRequest(ticket, assetRequestTicket);
    }

    private Budget findBudgetForPurchaseRequest(Ticket ticket, PurchaseRequestTicket purchaseRequestTicket) {
        if (Boolean.TRUE.equals(purchaseRequestTicket.getIsStandard())) {
            return findCompanyCommonBudget(ticket);
        }

        return findDepartmentBudget(ticket);
    }

    private Budget findBudgetForAssetRequest(Ticket ticket, AssetRequestTicket assetRequestTicket) {
        if (isStandardAssetRequest(assetRequestTicket)) {
            return findCompanyCommonBudget(ticket);
        }

        return findDepartmentBudget(ticket);
    }

    private Budget findCompanyCommonBudget(Ticket ticket) {
        return budgetRepository.findByCompany_IdAndDepartmentIsNullAndBudgetYear(
                        ticket.getCompany().getId(),
                        LocalDate.now().getYear()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회사 공통 예산이 없습니다."));
    }

    private Budget findDepartmentBudget(Ticket ticket) {
        return budgetRepository.findByCompany_IdAndDepartment_IdAndBudgetYear(
                        ticket.getCompany().getId(),
                        ticket.getDepartment().getId(),
                        LocalDate.now().getYear()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "해당 부서의 올해 예산이 없습니다."));
    }

    private boolean isStandardAssetRequest(AssetRequestTicket ticket) {
        if (ticket.getTangibleAssetItem() != null) {
            return Boolean.TRUE.equals(ticket.getTangibleAssetItem().getIsStandard());
        }

        return Boolean.TRUE.equals(ticket.getIntangibleAssetItem().getIsStandard());
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

        return getAvailableIntangibleSeatCount(companyId, ticket.getIntangibleAssetItem().getId()) >= ticket.getQuantity();
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

    private AssetRequestTicket findAssetRequestTicket(UUID ticketId, UUID companyId) {
        return assetRequestTicketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
    }

    private PurchaseRequestTicket findPurchaseRequestTicket(UUID ticketId, UUID companyId) {
        return purchaseRequestTicketRepository.findByIdAndCompany_Id(ticketId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
    }

    private BigDecimal resolvePurchaseRequestEstimatedAmount(PurchaseRequestTicket ticket) {
        return ticket.getExpectedPrice().multiply(BigDecimal.valueOf(ticket.getQuantity()));
    }

    private BigDecimal resolveEstimatedAmount(AssetRequestTicket ticket, UUID companyId) {
        BigDecimal unitPrice;
        if (ticket.getTangibleAssetItem() != null) {
            unitPrice = tangibleAssetRepository.findRecentPurchasePrices(
                            companyId,
                            ticket.getTangibleAssetItem().getId(),
                            PageRequest.of(0, 1)
                    )
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자산요청 예산 산정에 사용할 구매금액이 없습니다."));
        } else {
            unitPrice = intangibleAssetRepository.findRecentPurchasePrices(
                            companyId,
                            ticket.getIntangibleAssetItem().getId(),
                            PageRequest.of(0, 1)
                    )
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자산요청 예산 산정에 사용할 구매금액이 없습니다."));
        }

        return unitPrice.multiply(BigDecimal.valueOf(ticket.getQuantity()));
    }

    private BigDecimal getOutstandingHoldAmount(Ticket ticket, UUID companyId) {
        // 같은 티켓으로 여러 번 해제/집행되지 않도록 이력 기준 남은 선점액만 계산한다.
        BigDecimal holdIncrease = budgetHistoryRepository.sumAmountByTicketAndHistoryType(
                companyId,
                ticket.getId(),
                BudgetHistoryType.HOLD_INCREASE
        );
        BigDecimal holdDecrease = budgetHistoryRepository.sumAmountByTicketAndHistoryType(
                companyId,
                ticket.getId(),
                BudgetHistoryType.HOLD_DECREASE
        );

        return holdIncrease.subtract(holdDecrease);
    }

    private void validateAvailableBudget(Budget budget, BigDecimal amount) {
        if (budget.getAvailableAmount().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "가용 예산이 부족합니다.");
        }
    }

    private void settleActualExecution(
            Budget budget,
            Ticket ticket,
            PurchasePlan purchasePlan,
            BigDecimal holdAmount,
            BigDecimal actualAmount,
            String holdDescription,
            String useDescription
    ) {
        BigDecimal executionAmount = actualAmount.max(BigDecimal.ZERO);
        BigDecimal extraAmount = executionAmount.subtract(holdAmount);
        if (extraAmount.signum() > 0) {
            validateAvailableBudget(budget, extraAmount);
        }

        decreaseHold(budget, ticket, purchasePlan, holdAmount, holdDescription);
        if (executionAmount.signum() > 0) {
            increaseUsed(budget, ticket, purchasePlan, executionAmount, useDescription);
        }
    }

    private void decreaseHold(
            Budget budget,
            Ticket ticket,
            PurchasePlan purchasePlan,
            BigDecimal amount,
            String description
    ) {
        BigDecimal holdBefore = budget.getHeldAmount();
        BigDecimal usedBefore = budget.getUsedAmount();
        budget.decreaseHold(amount);

        saveHistory(
                budget,
                ticket,
                purchasePlan,
                BudgetHistoryType.HOLD_DECREASE,
                amount,
                usedBefore,
                budget.getUsedAmount(),
                holdBefore,
                budget.getHeldAmount(),
                description
        );
    }

    private void increaseUsed(
            Budget budget,
            Ticket ticket,
            PurchasePlan purchasePlan,
            BigDecimal amount,
            String description
    ) {
        BigDecimal holdBefore = budget.getHeldAmount();
        BigDecimal usedBefore = budget.getUsedAmount();
        budget.increaseUsed(amount);

        saveHistory(
                budget,
                ticket,
                purchasePlan,
                BudgetHistoryType.USE_INCREASE,
                amount,
                usedBefore,
                budget.getUsedAmount(),
                holdBefore,
                budget.getHeldAmount(),
                description
        );
    }

    private void saveHistory(
            Budget budget,
            Ticket ticket,
            PurchasePlan purchasePlan,
            BudgetHistoryType historyType,
            BigDecimal amount,
            BigDecimal usedAmountBefore,
            BigDecimal usedAmountAfter,
            BigDecimal holdAmountBefore,
            BigDecimal holdAmountAfter,
            String description
    ) {
        budgetHistoryRepository.save(BudgetHistory.builder()
                .company(budget.getCompany())
                .department(budget.getDepartment())
                .budget(budget)
                .ticket(ticket)
                .purchasePlan(purchasePlan)
                .historyType(historyType)
                .amount(amount)
                .usedAmountBefore(usedAmountBefore)
                .usedAmountAfter(usedAmountAfter)
                .holdAmountBefore(holdAmountBefore)
                .holdAmountAfter(holdAmountAfter)
                .totalBudget(budget.getTotalAmount())
                .description(description)
                .build());
    }
}
