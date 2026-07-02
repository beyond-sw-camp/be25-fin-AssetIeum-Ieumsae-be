package com.ieumsae.assetieum.domain.budget.budget.service;

import com.ieumsae.assetieum.global.common.util.KstDateTime;

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
import com.ieumsae.assetieum.domain.ticket.purchasereturn.entity.PurchaseReturnTicket;
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
        if (!isStandardAssetRequest(assetRequestTicket)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비표준 자산요청은 재고가 충분한 품목만 요청할 수 있습니다.");
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
                "자산 요청으로 인한 예산 집행 대기 금액"
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
                "구매 요청으로 인한 예산 집행 대기 금액"
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
        decreaseHold(budget, ticket, null, amount, "보유 재고 할당 후 집행 대기 금액 해제");
    }

    @Transactional
    public void reconcileHoldForAssetRequestPendingQuantity(Ticket ticket, UUID companyId, int pendingQuantity) {
        if (ticket.getTicketType() != TicketType.ASSET_REQUEST) {
            return;
        }

        AssetRequestTicket assetRequestTicket = findAssetRequestTicket(ticket.getId(), companyId);
        if (!isStandardAssetRequest(assetRequestTicket)) {
            return;
        }

        BigDecimal desiredHoldAmount = resolveEstimatedUnitPrice(assetRequestTicket, companyId)
                .multiply(BigDecimal.valueOf(Math.max(pendingQuantity, 0)));
        BigDecimal outstandingHoldAmount = getOutstandingHoldAmount(ticket, companyId);
        BigDecimal difference = desiredHoldAmount.subtract(outstandingHoldAmount);
        if (difference.signum() == 0) {
            return;
        }

        Budget budget = findBudgetForAssetRequest(ticket, assetRequestTicket);
        if (difference.signum() > 0) {
            validateAvailableBudget(budget, difference);
            increaseHold(budget, ticket, null, difference, "자산요청 미배정 수량 변경으로 인한 집행 대기 금액 증가");
            return;
        }

        decreaseHold(budget, ticket, null, difference.abs(), "자산요청 미배정 수량 변경으로 인한 집행 대기 금액 감소");
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
        decreaseHold(budget, ticket, null, amount, "티켓 취소로 인한 집행 대기 금액 해제");
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
            decreaseHold(budget, ticket, purchasePlan, amount, "구매 계획 취소로 인한 집행 대기 금액 해제");
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
            decreaseHold(budget, ticket, purchasePlan, amount, "구매 계획 실행으로 인한 집행 대기 금액 해제");
            increaseUsed(budget, ticket, purchasePlan, amount, "구매 계획 실행으로 인한 사용 금액");
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
                "실제 구매로 인한 집행 대기 금액 해제",
                "실제 결제 금액으로 예산 집행"
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
                "직접 구매로 인한 집행 대기 금액 해제",
                "직접 결제 금액으로 예산 집행"
        );
    }

    @Transactional
    public void adjustForDirectPurchaseResultUpdate(
            Ticket ticket,
            UUID companyId,
            BigDecimal previousActualAmount,
            BigDecimal updatedActualAmount
    ) {
        if (ticket.getTicketType() != TicketType.PURCHASE_REQUEST) {
            return;
        }

        BigDecimal previousAmount = previousActualAmount == null ? BigDecimal.ZERO : previousActualAmount.max(BigDecimal.ZERO);
        BigDecimal updatedAmount = updatedActualAmount == null ? BigDecimal.ZERO : updatedActualAmount.max(BigDecimal.ZERO);
        BigDecimal difference = updatedAmount.subtract(previousAmount);
        if (difference.signum() == 0) {
            return;
        }

        Budget budget = findBudgetForTicket(ticket, companyId);
        if (difference.signum() > 0) {
            validateAvailableBudget(budget, difference);
            increaseUsed(budget, ticket, null, difference, "실제 결제 금액 증가로 인한 사용 예산 집행");
            return;
        }

        BigDecimal recoveryAmount = difference.abs().min(budget.getUsedAmount());
        if (recoveryAmount.signum() > 0) {
            decreaseUsed(budget, ticket, null, recoveryAmount, "실제 결제 금액 감소로 인한 사용 예산 집행");
        }
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
        increaseUsed(budget, ticket, null, maintenanceCost, "유지보수로 인한 사내 공용 예산 집행");
    }

    @Transactional
    public void executeForIntangibleAssetBillingCycle(
            IntangibleAsset asset,
            LocalDate billingDate
    ) {
        if (asset == null
                || asset.getPurchasePrice() == null
                || asset.getPurchasePrice().signum() <= 0) {
            return;
        }

        Budget budget = findBudgetForIntangibleAssetBillingCycle(asset, billingDate);
        BigDecimal amount = asset.getPurchasePrice();
        validateAvailableBudget(budget, amount);
        increaseUsed(
                budget,
                null,
                null,
                amount,
                "무형자산 자동 결제 인한 예산 집행 - 자산번호: " + asset.getAssetCode()
        );
    }

    @Transactional
    public void recoverForPurchaseReturn(
            Ticket ticket,
            PurchaseReturnTicket purchaseReturnTicket
    ) {
        if (ticket.getTicketType() != TicketType.PURCHASE_RETURN) {
            return;
        }

        BigDecimal amount = resolvePurchaseReturnRecoveryAmount(purchaseReturnTicket);
        if (amount.signum() <= 0) {
            return;
        }

        Budget budget = findBudgetForPurchaseReturn(ticket, purchaseReturnTicket);
        BigDecimal recoveryAmount = amount.min(budget.getUsedAmount());
        if (recoveryAmount.signum() <= 0) {
            return;
        }
        decreaseUsed(budget, ticket, null, recoveryAmount, "구매 반품으로 인한 사용 예산 환불");
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

    private Budget findBudgetForPurchaseReturn(Ticket ticket, PurchaseReturnTicket purchaseReturnTicket) {
        if (isStandardPurchaseReturn(purchaseReturnTicket)) {
            return findCompanyCommonBudget(ticket);
        }

        return findDepartmentBudget(ticket);
    }

    private Budget findBudgetForIntangibleAssetBillingCycle(IntangibleAsset asset, LocalDate billingDate) {
        int budgetYear = billingDate == null ? KstDateTime.today().getYear() : billingDate.getYear();
        if (asset.getIntangibleAssetItem() != null
                && Boolean.TRUE.equals(asset.getIntangibleAssetItem().getIsStandard())) {
            return budgetRepository.findByCompany_IdAndDepartmentIsNullAndBudgetYear(
                            asset.getCompany().getId(),
                            budgetYear
                    )
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회사 공용 예산이 없습니다."));
        }

        if (asset.getDepartment() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "부서 비용 무형자산의 소유 부서가 없습니다.");
        }

        return budgetRepository.findByCompany_IdAndDepartment_IdAndBudgetYear(
                        asset.getCompany().getId(),
                        asset.getDepartment().getId(),
                        budgetYear
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "해당 부서의 예산이 없습니다."));
    }

    private Budget findCompanyCommonBudget(Ticket ticket) {
        return budgetRepository.findByCompany_IdAndDepartmentIsNullAndBudgetYear(
                        ticket.getCompany().getId(),
                        KstDateTime.today().getYear()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회사 공통 예산이 없습니다."));
    }

    private Budget findDepartmentBudget(Ticket ticket) {
        return budgetRepository.findByCompany_IdAndDepartment_IdAndBudgetYear(
                        ticket.getCompany().getId(),
                        ticket.getDepartment().getId(),
                        KstDateTime.today().getYear()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "해당 부서의 올해 예산이 없습니다."));
    }

    private boolean isStandardAssetRequest(AssetRequestTicket ticket) {
        if (ticket.getTangibleAssetItem() != null) {
            return Boolean.TRUE.equals(ticket.getTangibleAssetItem().getIsStandard());
        }

        return Boolean.TRUE.equals(ticket.getIntangibleAssetItem().getIsStandard());
    }

    private boolean isStandardPurchaseReturn(PurchaseReturnTicket ticket) {
        if (ticket.getTangibleAsset() != null) {
            return Boolean.TRUE.equals(ticket.getTangibleAsset().getTangibleAssetItem().getIsStandard());
        }

        return Boolean.TRUE.equals(ticket.getIntangibleAsset().getIntangibleAssetItem().getIsStandard());
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
        int shortageQuantity = resolveShortageQuantity(ticket, companyId);
        if (shortageQuantity <= 0) {
            return BigDecimal.ZERO;
        }

        return resolveEstimatedUnitPrice(ticket, companyId).multiply(BigDecimal.valueOf(shortageQuantity));
    }

    private BigDecimal resolveEstimatedUnitPrice(AssetRequestTicket ticket, UUID companyId) {
        BigDecimal unitPrice = ticket.getEstimatedUnitPrice();
        if (unitPrice != null) {
            return unitPrice;
        }

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

        return unitPrice;
    }

    private int resolveShortageQuantity(AssetRequestTicket ticket, UUID companyId) {
        int availableCount;
        if (ticket.getTangibleAssetItem() != null) {
            availableCount = Math.toIntExact(tangibleAssetRepository.countByCompany_IdAndTangibleAssetItem_IdAndTangibleAssetStatus(
                    companyId,
                    ticket.getTangibleAssetItem().getId(),
                    TangibleAssetStatus.AVAILABLE
            ));
        } else {
            availableCount = getAvailableIntangibleSeatCount(
                    companyId,
                    ticket.getIntangibleAssetItem().getId(),
                    ticket.getTicket().getDepartment().getId()
            );
        }
        return Math.max(ticket.getQuantity() - availableCount, 0);
    }

    private BigDecimal resolvePurchaseReturnRecoveryAmount(PurchaseReturnTicket ticket) {
        BigDecimal purchasePrice = ticket.getTangibleAsset() != null
                ? ticket.getTangibleAsset().getPurchasePrice()
                : ticket.getIntangibleAsset().getPurchasePrice();

        if (purchasePrice == null) {
            return BigDecimal.ZERO;
        }
        return purchasePrice.max(BigDecimal.ZERO);
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

    private void increaseHold(
            Budget budget,
            Ticket ticket,
            PurchasePlan purchasePlan,
            BigDecimal amount,
            String description
    ) {
        BigDecimal holdBefore = budget.getHeldAmount();
        BigDecimal usedBefore = budget.getUsedAmount();
        budget.increaseHold(amount);

        saveHistory(
                budget,
                ticket,
                purchasePlan,
                BudgetHistoryType.HOLD_INCREASE,
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

    private void decreaseUsed(
            Budget budget,
            Ticket ticket,
            PurchasePlan purchasePlan,
            BigDecimal amount,
            String description
    ) {
        BigDecimal holdBefore = budget.getHeldAmount();
        BigDecimal usedBefore = budget.getUsedAmount();
        budget.decreaseUsed(amount);

        saveHistory(
                budget,
                ticket,
                purchasePlan,
                BudgetHistoryType.RECOVERY,
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
