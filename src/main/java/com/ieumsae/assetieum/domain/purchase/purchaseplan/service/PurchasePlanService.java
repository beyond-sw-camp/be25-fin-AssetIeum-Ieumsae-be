package com.ieumsae.assetieum.domain.purchase.purchaseplan.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanCreateRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanDetailResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanSearchRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanUpdateStatusRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlan;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.repository.PurchasePlanRepository;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.dto.PurchasePlanItemCreateRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.repository.PurchasePlanItemRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.PurchaseRequestTicketRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchasePlanService {

    private static final String PURCHASE_PLAN_NO_PREFIX = "PLN";
    private static final String REDIS_KEY_PREFIX = "purchase-plan:no:";

    private final CodeGenerator codeGenerator;
    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final TangibleAssetItemRepository tangibleAssetItemRepository;
    private final IntangibleAssetItemRepository intangibleAssetItemRepository;
    private final PurchaseRequestTicketRepository purchaseRequestTicketRepository;
    private final PurchasePlanRepository purchasePlanRepository;
    private final PurchasePlanItemRepository purchasePlanItemRepository;

    @Transactional
    public PurchasePlanResponse createPurchasePlan(
            PurchasePlanCreateRequest request,
            AuthenticatedMember member
    ) {
        Company company = companyRepository.findById(member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member requester = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(member.id(), member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        BigDecimal estimatedAmount = calculateEstimatedAmount(request.getItems());
        PurchasePlan purchasePlan = purchasePlanRepository.save(PurchasePlan.builder()
                .company(company)
                .requester(requester)
                .planNo(codeGenerator.generate(PURCHASE_PLAN_NO_PREFIX, REDIS_KEY_PREFIX, member.companyId()))
                .purchaseRequestStatus(PurchaseRequestStatus.REQUESTED)
                .estimatedAmount(estimatedAmount)
                .itemCount(request.getItems().size())
                .build());

        List<PurchasePlanItem> purchasePlanItems = createPurchasePlanItems(
                request.getItems(),
                company,
                purchasePlan,
                member.companyId()
        );
        purchasePlanItemRepository.saveAll(purchasePlanItems);

        return PurchasePlanResponse.from(purchasePlan);
    }

    private BigDecimal calculateEstimatedAmount(List<PurchasePlanItemCreateRequest> items) {
        return items.stream()
                .map(item -> item.getEstimatedUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<PurchasePlanItem> createPurchasePlanItems(
            List<PurchasePlanItemCreateRequest> requests,
            Company company,
            PurchasePlan purchasePlan,
            UUID companyId
    ) {
        List<PurchasePlanItem> purchasePlanItems = new ArrayList<>();

        for (PurchasePlanItemCreateRequest request : requests) {
            TangibleAssetItem tangibleAssetItem = null;
            IntangibleAssetItem intangibleAssetItem = null;

            if (request.getAssetType() == AssetType.TANGIBLE) {
                tangibleAssetItem = findTangibleAssetItem(request.getAssetItemId(), companyId);
            } else if (request.getAssetType() == AssetType.INTANGIBLE) {
                intangibleAssetItem = findIntangibleAssetItem(request.getAssetItemId(), companyId);
            } else {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            purchasePlanItems.add(PurchasePlanItem.builder()
                    .company(company)
                    .purchasePlan(purchasePlan)
                    .purchaseRequestTicket(findPurchaseRequestTicket(request.getTicketId(), companyId))
                    .tangibleAssetItem(tangibleAssetItem)
                    .intangibleAssetItem(intangibleAssetItem)
                    .itemName(request.getItemName())
                    .department(findDepartment(request.getDepartmentId(), companyId))
                    .isStandard(request.getIsStandard())
                    .quantity(request.getQuantity())
                    .estimatedUnitPrice(request.getEstimatedUnitPrice())
                    .externalUrl(request.getExternalUrl())
                    .build());
        }

        return purchasePlanItems;
    }

    private Department findDepartment(UUID departmentId, UUID companyId) {
        if (departmentId == null) {
            return null;
        }

        return departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(departmentId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    private PurchaseRequestTicket findPurchaseRequestTicket(UUID ticketId, UUID companyId) {
        if (ticketId == null) {
            return null;
        }

        return purchaseRequestTicketRepository.findByIdAndCompany_Id(ticketId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
    }

    private TangibleAssetItem findTangibleAssetItem(UUID itemId, UUID companyId) {
        if (itemId == null) {
            return null;
        }

        return tangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(itemId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));
    }

    private IntangibleAssetItem findIntangibleAssetItem(UUID itemId, UUID companyId) {
        if (itemId == null) {
            return null;
        }

        return intangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(itemId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));
    }

    @Transactional
    public PurchasePlanResponse deletePurchasePlan(
            UUID planId,
            UUID companyId
    ) {

        // 1. 입력값 검증
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        PurchasePlan purchasePlan = purchasePlanRepository.findByIdAndDeletedAtIsNullAndCompany_Id(planId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_NOT_FOUND));

        if(purchasePlan.getPurchaseRequestStatus() != PurchaseRequestStatus.REQUESTED){
            throw new BusinessException(ErrorCode.PURCHASE_PLAN_DELETE_ONLY_REQUESTED);
        }

        // 2. 구매 계획 삭제 (soft delete)
        purchasePlan.delete();

        return PurchasePlanResponse.from(purchasePlan);

    }

    public PaginationResponse<PurchasePlanResponse> getPurchasePlans(
            PurchasePlanSearchRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member requester = null;
        if(request.getRequesterId() != null){
            requester = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getRequesterId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        }

        // 2. 페이징 처리 및 필터링 후 구매 계획 목록 반환
        Page<PurchasePlanResponse> purchasePlanPage = purchasePlanRepository.search(
                companyId,
                request.getStatus(),
                request.getRequesterId(),
                request.getKeyword(),
                request.toPageable()
        );

        return PaginationResponse.from(purchasePlanPage);
    }

    public PurchasePlanDetailResponse getPurchasePlanDetail(
            UUID planId,
            UUID companyId
    ) {
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        PurchasePlan purchasePlan = purchasePlanRepository.findByIdAndDeletedAtIsNullAndCompany_Id(planId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_NOT_FOUND));

        List<PurchasePlanItem> purchasePlanItems =
                purchasePlanItemRepository.findAllByPurchasePlan_IdAndCompany_Id(planId, companyId);

        return PurchasePlanDetailResponse.from(purchasePlan, purchasePlanItems);
    }

    @Transactional
    public PurchasePlanResponse updatePurchasePlanStatus(
            UUID planId,
            PurchasePlanUpdateStatusRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        PurchasePlan purchasePlan = purchasePlanRepository.findByIdAndDeletedAtIsNullAndCompany_Id(planId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_NOT_FOUND));


        // 2. 상태 변경
        validateStatusTransition(purchasePlan.getPurchaseRequestStatus(), request.getStatus());
        purchasePlan.updateStatus(request.getStatus());

        return PurchasePlanResponse.from(purchasePlan);

    }

    private void validateStatusTransition(
            PurchaseRequestStatus currentStatus,
            PurchaseRequestStatus nextStatus
    ) {
        if (currentStatus == nextStatus) {
            return;
        }

        boolean isValid = switch (currentStatus) {
            case REQUESTED -> nextStatus == PurchaseRequestStatus.APPROVED
                    || nextStatus == PurchaseRequestStatus.REJECTED;
            case APPROVED -> nextStatus == PurchaseRequestStatus.ORDERED;
            case ORDERED -> nextStatus == PurchaseRequestStatus.DELIVERED;
            case DELIVERED -> nextStatus == PurchaseRequestStatus.COMPLETED;
            case REJECTED, COMPLETED, CANCELLED -> false;
        };

        if (!isValid) {
            throw new BusinessException(ErrorCode.PURCHASE_PLAN_INVALID_STATUS_TRANSITION);
        }
    }
}
