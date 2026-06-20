package com.ieumsae.assetieum.domain.purchase.purchaseplan.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.asset.dto.IntangibleAssetResponse;
import com.ieumsae.assetieum.domain.intangibleasset.asset.service.IntangibleAssetService;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.dto.IntangibleAssetAssignmentRequest;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.service.IntangibleAssetAssignmentService;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.service.IntangibleAssetItemService;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanCreateRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanDetailResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanItemCreateIntangibleAssetRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanItemCreateItemRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanItemCreateTangibleAssetRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanSearchRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanStatisticResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.dto.PurchasePlanUpdateStatusRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlan;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.repository.PurchasePlanRepository;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchasePlanItemStatus;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.type.PurchaseRequestStatus;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.dto.PurchasePlanItemCreateRequest;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.dto.PurchasePlanItemResponse;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.repository.PurchasePlanItemRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.service.TangibleAssetService;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.service.TangibleAssetItemService;
import com.ieumsae.assetieum.domain.ticket.assetrequest.repository.AssetRequestTicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.PurchaseRequestTicketRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
    private final IntangibleAssetCategoryRepository intangibleAssetCategoryRepository;
    private final TangibleAssetItemRepository tangibleAssetItemRepository;
    private final IntangibleAssetItemRepository intangibleAssetItemRepository;
    private final AssetRequestTicketRepository assetRequestTicketRepository;
    private final PurchaseRequestTicketRepository purchaseRequestTicketRepository;
    private final TicketRepository ticketRepository;
    private final PurchasePlanRepository purchasePlanRepository;
    private final PurchasePlanItemRepository purchasePlanItemRepository;
    private final TangibleAssetItemService tangibleAssetItemService;
    private final IntangibleAssetItemService intangibleAssetItemService;
    private final TangibleAssetService tangibleAssetService;
    private final IntangibleAssetService intangibleAssetService;
    private final IntangibleAssetAssignmentService intangibleAssetAssignmentService;

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

            // Validate and link the common ticket; both asset request and team purchase request are allowed.
            Ticket linkedTicket = findLinkedPurchasePlanTicket(request.getTicketId(), companyId);
            purchasePlanItems.add(PurchasePlanItem.builder()
                    .company(company)
                    .purchasePlan(purchasePlan)
                    .ticket(linkedTicket)
                    .assetType(request.getAssetType())
                    .tangibleAssetItem(tangibleAssetItem)
                    .intangibleAssetItem(intangibleAssetItem)
                    .productName(request.getProductName())
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

    private Ticket findLinkedPurchasePlanTicket(UUID ticketId, UUID companyId) {
        if (ticketId == null) {
            return null;
        }

        Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
        if (ticket.getTicketStatus() != TicketStatus.ASSET_APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매자산팀 승인 상태의 티켓만 구매 계획에 추가할 수 있습니다.");
        }
        // Asset request tickets are valid purchase plan sources after asset-team approval.
        if (ticket.getTicketType() == TicketType.ASSET_REQUEST) {
            assetRequestTicketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
            return ticket;
        }
        if (ticket.getTicketType() != TicketType.PURCHASE_REQUEST) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자산요청 또는 구매요청 티켓만 구매 계획에 추가할 수 있습니다.");
        }

        // Purchase request tickets must be team-purchase requests to be grouped into a purchase plan.
        PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository.findByIdAndCompany_Id(ticketId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
        if (purchaseRequestTicket.getRequestMethod() != RequestMethod.TEAM_PURCHASE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매자산팀 구매 티켓만 구매 계획에 추가할 수 있습니다.");
        }

        return ticket;
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

        if(request.getRequesterId() != null){
            memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getRequesterId(), companyId)
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
                purchasePlanItemRepository.findAllByPurchasePlan_IdAndCompany_Id(planId, companyId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_ITEM_NOT_FOUND));

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
        startLinkedTicketsIfApproved(purchasePlan, request.getStatus(), companyId);

        return PurchasePlanResponse.from(purchasePlan);

    }

    private void startLinkedTicketsIfApproved(
            PurchasePlan purchasePlan,
            PurchaseRequestStatus status,
            UUID companyId
    ) {
        if (status != PurchaseRequestStatus.APPROVED) {
            return;
        }

        List<PurchasePlanItem> items = purchasePlanItemRepository.findAllByPurchasePlan_IdAndCompany_Id(
                purchasePlan.getId(),
                companyId
        )
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_ITEM_NOT_FOUND));

        for (PurchasePlanItem item : items) {
            Ticket linkedTicket = item.getTicket();
            if (linkedTicket == null) {
                continue;
            }
            Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(
                    linkedTicket.getId(),
                    companyId
            ).orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
            if (ticket.getTicketStatus() == TicketStatus.ASSET_APPROVED) {
                ticket.changeProcessingStatus(TicketStatus.IN_PROGRESS, java.time.LocalDateTime.now());
            }
        }
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

    public PurchasePlanStatisticResponse getPurchasePlanStatistics(UUID companyId) {

        // 1. 입력값 검증
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. 통계값 반환
        return purchasePlanRepository.getPurchasePlanStatistics(companyId);

    }

    @Transactional
    public PurchasePlanItemResponse updatePurchasePlanItemStatus(
            UUID planId,
            Long itemId,
            UUID companyId
    ) {
        // 1. 입력값 확인
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        PurchasePlan purchasePlan = purchasePlanRepository.findByIdAndDeletedAtIsNullAndCompany_Id(planId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_NOT_FOUND));

        PurchasePlanItem purchasePlanItem = purchasePlanItemRepository.findByIdAndPurchasePlan_IdAndCompany_Id(itemId, planId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_ITEM_NOT_FOUND));

        List<PurchasePlanItem> purchasePlanItems = purchasePlanItemRepository.findAllByPurchasePlan_IdAndCompany_Id(planId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_ITEM_NOT_FOUND));

        // 2. 납품 확인으로 상태 변경
        // 모든 상품이 납품 확인이 되었으면 해당 plan 의 상태도 delivered 로 변경

        validateStatusTransition(purchasePlan.getPurchaseRequestStatus(), PurchaseRequestStatus.DELIVERED);
        if (purchasePlanItem.getPurchasePlanItemStatus() != PurchasePlanItemStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        purchasePlanItem.updateStatus();

        for (PurchasePlanItem item : purchasePlanItems) {
            if (item.getPurchasePlanItemStatus() == PurchasePlanItemStatus.PENDING) {
                return PurchasePlanItemResponse.from(purchasePlanItem);
            }
        }

        purchasePlan.updateStatus(PurchaseRequestStatus.DELIVERED);

        return PurchasePlanItemResponse.from(purchasePlanItem);
    }

    /**
     * 구매 계획에서 품목 등록
     */
    @Transactional
    public void createItemFromPurchasePlan(
            UUID planId,
            Long itemId,
            UUID companyId,
            PurchasePlanItemCreateItemRequest request
    ) {
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        purchasePlanRepository.findByIdAndDeletedAtIsNullAndCompany_Id(planId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_NOT_FOUND));

        PurchasePlanItem purchasePlanItem = purchasePlanItemRepository.findByIdAndPurchasePlan_IdAndCompany_Id(itemId, planId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_ITEM_NOT_FOUND));

        if(purchasePlanItem.getTangibleAssetItem() != null || purchasePlanItem.getIntangibleAssetItem() != null) {
            throw new BusinessException(ErrorCode.PURCHASE_PLAN_ITEM_ALREADY_HAS_ITEM_ID);
        }
        
        validateCreateItemRequest(purchasePlanItem.getAssetType(), request);

        if (purchasePlanItem.getAssetType() == AssetType.TANGIBLE) {
            TangibleAssetItem tangibleAssetItem = findOrCreateTangibleItem(purchasePlanItem, request, companyId);
            purchasePlanItem.attachTangibleAssetItem(tangibleAssetItem);
            purchasePlanItemRepository.save(purchasePlanItem);
            return;
        }

        if (purchasePlanItem.getAssetType() == AssetType.INTANGIBLE) {
            IntangibleAssetItem intangibleAssetItem = findOrCreateIntangibleItem(purchasePlanItem, request, companyId);
            purchasePlanItem.attachIntangibleAssetItem(intangibleAssetItem);
            purchasePlanItemRepository.save(purchasePlanItem);
            return;
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private void validateCreateItemRequest(AssetType assetType, PurchasePlanItemCreateItemRequest request) {
        if (request.getCategoryId() == null || request.getIsStandard() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (assetType == AssetType.TANGIBLE) {
            if (!StringUtils.hasText(request.getManufacturer()) || !StringUtils.hasText(request.getModelName())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (StringUtils.hasText(request.getProvider()) || request.getLicenseType() != null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            return;
        }

        if (assetType == AssetType.INTANGIBLE) {
            if (!StringUtils.hasText(request.getProvider()) || request.getLicenseType() == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (StringUtils.hasText(request.getManufacturer()) || StringUtils.hasText(request.getModelName())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            return;
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private TangibleAssetCategory resolveTangibleCategory(
            PurchasePlanItemCreateItemRequest request,
            UUID companyId
    ) {
        return tangibleAssetCategoryRepository.findByIdAndCompany_Id(request.getCategoryId(), companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));
    }

    private TangibleAssetItem findOrCreateTangibleItem(
            PurchasePlanItem purchasePlanItem,
            PurchasePlanItemCreateItemRequest request,
            UUID companyId
    ) {
        if (tangibleAssetItemRepository.existsByCompany_IdAndProductName(companyId, purchasePlanItem.getProductName())) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
        }

        TangibleAssetCategory category = resolveTangibleCategory(request, companyId);
        TangibleAssetItemCreateRequest createRequest = new TangibleAssetItemCreateRequest(
                category.getId(),
                purchasePlanItem.getProductName(),
                request.getManufacturer().trim(),
                request.getModelName().trim(),
                resolveBoolean(request.getIsStandard(), purchasePlanItem.getIsStandard())
        );
        tangibleAssetItemService.createItem(createRequest, companyId);
        return tangibleAssetItemRepository.findByProductNameAndCompany_IdAndDeletedAtIsNull(
                purchasePlanItem.getProductName(),
                companyId
        ).orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));
    }

    private IntangibleAssetCategory resolveIntangibleCategory(
            PurchasePlanItemCreateItemRequest request,
            UUID companyId
    ) {
        return intangibleAssetCategoryRepository.findByIdAndCompany_Id(request.getCategoryId(), companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND));
    }

    private IntangibleAssetItem findOrCreateIntangibleItem(
            PurchasePlanItem purchasePlanItem,
            PurchasePlanItemCreateItemRequest request,
            UUID companyId
    ) {
        if (intangibleAssetItemRepository.existsByCompany_IdAndProductName(companyId, purchasePlanItem.getProductName())) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
        }

        IntangibleAssetCategory category = resolveIntangibleCategory(request, companyId);
        IntangibleAssetItemCreateRequest createRequest = new IntangibleAssetItemCreateRequest(
                category.getId(),
                purchasePlanItem.getProductName(),
                request.getProvider().trim(),
                request.getLicenseType(),
                resolveBoolean(request.getIsStandard(), purchasePlanItem.getIsStandard())
        );
        intangibleAssetItemService.createItem(createRequest, companyId);
        return intangibleAssetItemRepository.findByProductNameAndCompany_IdAndDeletedAtIsNull(
                purchasePlanItem.getProductName(),
                companyId
        ).orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));
    }

    private Boolean resolveBoolean(Boolean requestedValue, Boolean fallbackValue) {
        if (requestedValue != null) {
            return requestedValue;
        }

        return fallbackValue != null ? fallbackValue : Boolean.TRUE;
    }

    @Transactional
    public void createTangibleAssetFromPurchasePlan(
            UUID planId,
            Long itemId,
            UUID companyId,
            @Valid PurchasePlanItemCreateTangibleAssetRequest request
    ) {
        PurchasePlanItem purchasePlanItem = findPurchasePlanItemForAssetCreation(planId, itemId, companyId);
        validateAssetCreationTarget(purchasePlanItem, AssetType.TANGIBLE);
        validateTangibleAssetCreationRequest(purchasePlanItem, request);

        TangibleAssetItem item = resolveTangibleItemForAssetCreation(purchasePlanItem);
        List<UUID> memberIds = normalizeMemberIds(request.getMemberIds(), purchasePlanItem.getQuantity());

        for (int i = 0; i < purchasePlanItem.getQuantity(); i++) {
            tangibleAssetService.createAsset(TangibleAssetCreateRequest.builder()
                    .tangibleItemId(item.getId())
                    .serialNumber(request.getSerialNumbers().get(i).trim())
                    .usageType(request.getUsageType())
                    .assetUsageType(request.getAssetUsageType())
                    .memberId(memberIds.get(i))
                    .departmentId(request.getDepartmentId())
                    .location(request.getLocation())
                    .usedStartedAt(request.getUsedStartedAt())
                    .returnDueDate(request.getReturnDueDate())
                    .purchaseDate(request.getPurchaseDate())
                    .purchasePrice(request.getPurchasePrice())
                    .purchaseVendor(request.getPurchaseVendor())
                    .warrantyExpiredAt(request.getWarrantyExpiredAt())
                    .build(), companyId);
        }

        purchasePlanItem.markAssetRegistered();
    }

    @Transactional
    public void createIntangibleAssetFromPurchasePlan(
            UUID planId,
            Long itemId,
            UUID companyId,
            @Valid PurchasePlanItemCreateIntangibleAssetRequest request
    ) {
        PurchasePlanItem purchasePlanItem = findPurchasePlanItemForAssetCreation(planId, itemId, companyId);
        validateAssetCreationTarget(purchasePlanItem, AssetType.INTANGIBLE);
        validateIntangibleAssetCreationRequest(purchasePlanItem, request);

        IntangibleAssetItem item = resolveIntangibleItemForAssetCreation(purchasePlanItem);

        List<String> licenseCodes = normalizeLicenseCodes(request, purchasePlanItem.getQuantity());
        List<List<UUID>> memberIdsByAsset = normalizeIntangibleMemberIds(
                request.getMemberIds(),
                purchasePlanItem.getQuantity(),
                request.getSeatCount()
        );
        for (int i = 0; i < purchasePlanItem.getQuantity(); i++) {
            IntangibleAssetResponse asset = intangibleAssetService.createAsset(IntangibleAssetCreateRequest.builder()
                    .intangibleItemId(item.getId())
                    .licenseCode(licenseCodes.get(i))
                    .seatCount(request.getSeatCount())
                    .isAutoRenewal(request.getIsAutoRenewal())
                    .purchaseDate(request.getPurchaseDate())
                    .purchasePrice(request.getPurchasePrice())
                    .purchaseVendor(request.getPurchaseVendor())
                    .departmentId(request.getDepartmentId())
                    .startedAt(request.getStartedAt())
                    .expiredAt(request.getExpiredAt())
                    .billingCycle(request.getBillingCycle())
                    .build(), companyId);

            assignIntangibleAssetMembers(asset.getIntangibleAssetId(), memberIdsByAsset.get(i), request, companyId);
        }

        purchasePlanItem.markAssetRegistered();
    }

    private PurchasePlanItem findPurchasePlanItemForAssetCreation(UUID planId, Long itemId, UUID companyId) {
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        purchasePlanRepository.findByIdAndDeletedAtIsNullAndCompany_Id(planId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_NOT_FOUND));

        return purchasePlanItemRepository.findByIdAndPurchasePlan_IdAndCompany_Id(itemId, planId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PURCHASE_PLAN_ITEM_NOT_FOUND));
    }

    private void validateAssetCreationTarget(PurchasePlanItem purchasePlanItem, AssetType assetType) {
        if (purchasePlanItem.getAssetType() != assetType) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (purchasePlanItem.getPurchasePlanItemStatus() != PurchasePlanItemStatus.RECEIVED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자산을 등록할 수 없는 상태의 품목입니다.");
        }
    }

    private void validateTangibleAssetCreationRequest(
            PurchasePlanItem purchasePlanItem,
            PurchasePlanItemCreateTangibleAssetRequest request
    ) {
        if (request.getSerialNumbers() == null || request.getSerialNumbers().size() != purchasePlanItem.getQuantity()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "품목 수량만큼만 자산을 등록할 수 있습니다.");
        }

        long distinctSerialCount = request.getSerialNumbers().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .count();

        if (distinctSerialCount != purchasePlanItem.getQuantity()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "품목 수량만큼만 자산을 등록할 수 있습니다.");
        }
    }

    private void validateIntangibleAssetCreationRequest(
            PurchasePlanItem purchasePlanItem,
            PurchasePlanItemCreateIntangibleAssetRequest request
    ) {
        if (request.getLicenseCodes() == null || request.getLicenseCodes().isEmpty()) {
            return;
        }

        if (request.getLicenseCodes().size() != purchasePlanItem.getQuantity()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "품목 수량만큼만 자산을 등록할 수 있습니다.");
        }

        long distinctLicenseCodeCount = request.getLicenseCodes().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .count();

        if (distinctLicenseCodeCount != purchasePlanItem.getQuantity()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private TangibleAssetItem resolveTangibleItemForAssetCreation(PurchasePlanItem purchasePlanItem) {
        if (purchasePlanItem.getTangibleAssetItem() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return purchasePlanItem.getTangibleAssetItem();
    }

    private IntangibleAssetItem resolveIntangibleItemForAssetCreation(PurchasePlanItem purchasePlanItem) {
        if (purchasePlanItem.getIntangibleAssetItem() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return purchasePlanItem.getIntangibleAssetItem();
    }

    private List<String> normalizeLicenseCodes(
            PurchasePlanItemCreateIntangibleAssetRequest request,
            Integer quantity
    ) {
        if (request.getLicenseCodes() == null || request.getLicenseCodes().isEmpty()) {
            List<String> emptyLicenseCodes = new ArrayList<>();
            for (int i = 0; i < quantity; i++) {
                emptyLicenseCodes.add(null);
            }
            return emptyLicenseCodes;
        }

        return request.getLicenseCodes().stream()
                .map(String::trim)
                .toList();
    }

    private List<UUID> normalizeMemberIds(List<UUID> memberIds, Integer quantity) {
        if (memberIds == null || memberIds.isEmpty()) {
            List<UUID> emptyMemberIds = new ArrayList<>();
            for (int i = 0; i < quantity; i++) {
                emptyMemberIds.add(null);
            }
            return emptyMemberIds;
        }

        if (memberIds.size() != quantity) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "품목 수량만큼만 자산을 등록할 수 있습니다.");
        }

        return memberIds;
    }

    private List<List<UUID>> normalizeIntangibleMemberIds(
            List<List<UUID>> memberIds,
            Integer quantity,
            Integer seatCount
    ) {
        if (memberIds == null || memberIds.isEmpty()) {
            List<List<UUID>> emptyMemberIds = new ArrayList<>();
            for (int i = 0; i < quantity; i++) {
                emptyMemberIds.add(List.of());
            }
            return emptyMemberIds;
        }

        if (memberIds.size() != quantity) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "?덈ぉ ?섎웾留뚰겮留??먯궛???깅줉?????덉뒿?덈떎.");
        }

        List<List<UUID>> normalizedMemberIds = new ArrayList<>();
        for (List<UUID> assetMemberIds : memberIds) {
            if (assetMemberIds == null || assetMemberIds.isEmpty()) {
                normalizedMemberIds.add(List.of());
                continue;
            }

            long distinctMemberCount = assetMemberIds.stream()
                    .filter(memberId -> memberId != null)
                    .distinct()
                    .count();

            if (distinctMemberCount != assetMemberIds.size() || assetMemberIds.size() > seatCount) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            normalizedMemberIds.add(assetMemberIds);
        }

        return normalizedMemberIds;
    }

    private void assignIntangibleAssetMembers(
            UUID assetId,
            List<UUID> memberIds,
            PurchasePlanItemCreateIntangibleAssetRequest request,
            UUID companyId
    ) {
        for (UUID memberId : memberIds) {
            intangibleAssetAssignmentService.assignAsset(
                    assetId,
                    IntangibleAssetAssignmentRequest.of(memberId, request.getExpiredAt()),
                    companyId
            );
        }
    }
}
