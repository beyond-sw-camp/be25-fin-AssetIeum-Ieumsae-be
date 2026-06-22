package com.ieumsae.assetieum.domain.ticket.purchaserequest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.budget.budget.service.BudgetExecutionService;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.type.LicenseType;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.repository.PurchasePlanItemRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.AssetUsageType;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketRequesterResolver;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseAssetAssignRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseAssetAssignResponse;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseResultCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseResultCreateResponse;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.DirectPurchaseResult;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.DirectPurchaseResultRepository;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.PurchaseRequestTicketRepository;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.type.ConfirmationStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseRequestTicketService {

	private static final String TANGIBLE_ASSET_CODE_PREFIX = "TA";
	private static final String TANGIBLE_ASSET_REDIS_KEY_PREFIX = "tangible-asset:code:";
	private static final String INTANGIBLE_ASSET_CODE_PREFIX = "IA";
	private static final String INTANGIBLE_ASSET_REDIS_KEY_PREFIX = "intangible-asset:code:";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
	};

	private final TicketRepository ticketRepository;
	private final PurchaseRequestTicketRepository purchaseRequestTicketRepository;
	private final DirectPurchaseResultRepository directPurchaseResultRepository;
	private final TangibleAssetRepository tangibleAssetRepository;
	private final IntangibleAssetRepository intangibleAssetRepository;
	private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;
	private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
	private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
	private final IntangibleAssetCategoryRepository intangibleAssetCategoryRepository;
	private final TangibleAssetItemRepository tangibleAssetItemRepository;
	private final IntangibleAssetItemRepository intangibleAssetItemRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TicketRequesterResolver ticketRequesterResolver;
	private final PurchaseRequestActionResolver purchaseRequestActionResolver;
	private final PurchasePlanItemRepository purchasePlanItemRepository;
	private final BudgetExecutionService budgetExecutionService;
	private final CodeGenerator codeGenerator;

	@Transactional
	public PurchaseRequestTicketCreateResponse createTeamPurchaseRequestTicket(
		AuthenticatedMember authenticatedMember,
		PurchaseRequestTicketCreateRequest request
	) {
		return createPurchaseRequestTicket(
			authenticatedMember,
			RequestMethod.TEAM_PURCHASE,
			request.getRequestedUsageType(),
			request.getAssetType(),
			request.getCategoryId(),
			request.getRequestedItemDetail(),
			request.getManufacturer(),
			request.getLicenseType(),
			request.getPurchaseUrl(),
			request.getQuantity(),
			request.getExpectedPrice(),
			request.getRequestReason()
		);
	}

	@Transactional
	public PurchaseRequestTicketCreateResponse createDirectPurchaseRequestTicket(
		AuthenticatedMember authenticatedMember,
		DirectPurchaseRequestTicketCreateRequest request
	) {
		DirectPurchaseTarget target = resolveDirectPurchaseTarget(request, authenticatedMember.companyId());
		return createPurchaseRequestTicket(
			authenticatedMember,
			RequestMethod.DIRECT_PURCHASE,
			request.getRequestedUsageType(),
			request.getAssetType(),
			request.getIsStandard(),
			target.tangibleAssetItem(),
			target.intangibleAssetItem(),
			target.tangibleAssetCategory(),
			target.intangibleAssetCategory(),
			target.requestedItemDetail(),
			target.manufacturer(),
			target.licenseType(),
			null,
			request.getQuantity(),
			request.getExpectedPrice(),
			request.getRequestReason()
		);
	}

	@Transactional
	public DirectPurchaseResultCreateResponse createDirectPurchaseResult(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		DirectPurchaseResultCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member submitter = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository.findByIdAndCompany_Id(
				ticketId,
				companyId
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		AssetType assetType = resolveAssetType(purchaseRequestTicket);

		validateDirectPurchaseResultTarget(purchaseRequestTicket, ticket, submitter);
		validateDirectPurchaseResultRequest(companyId, assetType, purchaseRequestTicket.getQuantity(), request, null);

		DirectPurchaseResult result = directPurchaseResultRepository.save(DirectPurchaseResult.create(
			purchaseRequestTicket,
			submitter,
			request.getActualPrice(),
			request.getPurchaseDate(),
			normalize(request.getPurchaseVendor()),
			assetType == AssetType.TANGIBLE
				? resolveDirectPurchaseSerialNumberStorage(companyId, purchaseRequestTicket.getQuantity(), request, null)
				: null,
			normalize(request.getLocation()),
			request.getWarrantyExpiredAt(),
			assetType == AssetType.INTANGIBLE
				? resolveDirectPurchaseLicenseCodeStorage(companyId, purchaseRequestTicket.getQuantity(), request, null)
				: null,
			request.getSeatCount(),
			request.getIsAutoRenewal(),
			request.getStartedAt(),
			request.getExpiredAt(),
			request.getBillingCycle()
		));
		budgetExecutionService.executeForDirectPurchaseResult(ticket, companyId, request.getActualPrice());
		// [수정] 자산팀 확인 전까지 IN_PROGRESS 상태 유지

		return DirectPurchaseResultCreateResponse.from(ticket, result, assetType);
	}

	@Transactional
	public DirectPurchaseResultCreateResponse updateDirectPurchaseResult(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		DirectPurchaseResultCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member submitter = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		DirectPurchaseResult result = directPurchaseResultRepository.findByIdAndCompany_Id(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		PurchaseRequestTicket purchaseRequestTicket = result.getPurchaseRequestTicket();
		AssetType assetType = resolveAssetType(purchaseRequestTicket);

		validateDirectPurchaseResultUpdatable(purchaseRequestTicket, ticket, submitter);
		// [수정] 이미 확인(CONFIRMED)된 영수증은 수정 불가
		if (result.getConfirmationStatus() == ConfirmationStatus.CONFIRMED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 확인 완료된 직접구매 정보는 수정할 수 없습니다.");
		}
		validateDirectPurchaseResultRequest(companyId, assetType, purchaseRequestTicket.getQuantity(), request, result);

		BigDecimal previousActualPrice = result.getActualPrice();
		result.update(
			request.getActualPrice(),
			request.getPurchaseDate(),
			normalize(request.getPurchaseVendor()),
			assetType == AssetType.TANGIBLE
				? resolveDirectPurchaseSerialNumberStorage(companyId, purchaseRequestTicket.getQuantity(), request, result)
				: null,
			normalize(request.getLocation()),
			request.getWarrantyExpiredAt(),
			assetType == AssetType.INTANGIBLE
				? resolveDirectPurchaseLicenseCodeStorage(companyId, purchaseRequestTicket.getQuantity(), request, result)
				: null,
			request.getSeatCount(),
			request.getIsAutoRenewal(),
			request.getStartedAt(),
			request.getExpiredAt(),
			request.getBillingCycle()
		);
		budgetExecutionService.adjustForDirectPurchaseResultUpdate(
			ticket,
			companyId,
			previousActualPrice,
			request.getActualPrice()
		);

		return DirectPurchaseResultCreateResponse.from(ticket, result, assetType);
	}

	public DirectPurchaseResultCreateResponse getDirectPurchaseResult(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member member = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		DirectPurchaseResult result = directPurchaseResultRepository.findByIdAndCompany_Id(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		PurchaseRequestTicket purchaseRequestTicket = result.getPurchaseRequestTicket();
		Ticket ticket = purchaseRequestTicket.getTicket();

		validateDirectPurchaseResultReadable(ticket, member);

		return DirectPurchaseResultCreateResponse.from(
			ticket,
			result,
			resolveAssetType(purchaseRequestTicket)
		);
	}

	@Transactional
	public DirectPurchaseResultCreateResponse confirmDirectPurchaseResult(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member member = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		DirectPurchaseResult result = directPurchaseResultRepository.findByIdAndCompany_Id(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		PurchaseRequestTicket purchaseRequestTicket = result.getPurchaseRequestTicket();

		validateDirectPurchaseResultReadable(ticket, member);
		validateDirectPurchaseAssetAssignee(ticket, member);

		if (result.getConfirmationStatus() == ConfirmationStatus.CONFIRMED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 확인완료 처리된 직접구매 결과입니다.");
		}

		result.confirm();

		return DirectPurchaseResultCreateResponse.from(ticket, result, resolveAssetType(purchaseRequestTicket));
	}

	@Transactional
	public DirectPurchaseAssetAssignResponse assignDirectPurchaseAsset(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		DirectPurchaseAssetAssignRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member assignee = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Ticket ticket = ticketRepository.findWithLockByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository.findByIdAndCompany_Id(
				ticketId,
				companyId
			)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		DirectPurchaseResult result = directPurchaseResultRepository.findByIdAndCompany_Id(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		AssetType assetType = resolveAssetType(purchaseRequestTicket);

		validateDirectPurchaseAssetAssignee(ticket, assignee);
		validateDirectPurchaseAssetAssignable(purchaseRequestTicket, ticket, result);
		if (assetType == AssetType.TANGIBLE) {
			return assignDirectPurchaseTangibleAsset(companyId, ticket, purchaseRequestTicket, result, request);
		}
		return assignDirectPurchaseIntangibleAsset(companyId, ticket, purchaseRequestTicket, result, request);
	}

	public PurchaseRequestTicketDetailResponse getPurchaseRequestTicket(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository
			.findByIdAndCompany_Id(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
		Ticket ticket = purchaseRequestTicket.getTicket();
		PurchasePlanItem linkedPurchasePlanItem = findLinkedPurchasePlanItem(ticket.getId(), companyId);
		DirectPurchaseResult directPurchaseResult = findDirectPurchaseResult(ticket.getId(), companyId);

		purchaseRequestActionResolver.validateReadable(ticket, viewer);

		return PurchaseRequestTicketDetailResponse.from(
			ticket,
			purchaseRequestTicket,
			viewer.getRole(),
			ticket.getRequester().getId().equals(viewer.getId()),
			resolveLinkedPurchasePlanId(linkedPurchasePlanItem),
			linkedPurchasePlanItem,
			directPurchaseResult,
			purchaseRequestActionResolver.createActions(ticket, viewer)
		);
	}

	private PurchasePlanItem findLinkedPurchasePlanItem(UUID ticketId, UUID companyId) {
		return purchasePlanItemRepository.findFirstByTicket_IdAndCompany_IdOrderByIdAsc(ticketId, companyId)
			.orElse(null);
	}

	private UUID resolveLinkedPurchasePlanId(PurchasePlanItem linkedPurchasePlanItem) {
		return linkedPurchasePlanItem == null ? null : linkedPurchasePlanItem.getPurchasePlan().getId();
	}

	private DirectPurchaseResult findDirectPurchaseResult(UUID ticketId, UUID companyId) {
		return directPurchaseResultRepository.findByIdAndCompany_Id(ticketId, companyId)
			.orElse(null);
	}

	private DirectPurchaseAssetAssignResponse assignDirectPurchaseTangibleAsset(
		UUID companyId,
		Ticket ticket,
		PurchaseRequestTicket purchaseRequestTicket,
		DirectPurchaseResult result,
		DirectPurchaseAssetAssignRequest request
	) {
		TangibleAssetItem item = resolveDirectPurchaseTangibleItem(companyId, purchaseRequestTicket, request);
		List<String> serialNumbers = resolveDirectPurchaseSerialNumbers(
			companyId,
			item.getId(),
			purchaseRequestTicket.getQuantity(),
			result,
			request
		);
		BigDecimal unitPrice = divideActualPrice(result.getActualPrice(), purchaseRequestTicket.getQuantity());
		List<DirectPurchaseAssetAssignResponse.AssignedAssetResponse> assignedAssets = new ArrayList<>();

		for (String serialNumber : serialNumbers) {
			TangibleAsset asset = tangibleAssetRepository.save(TangibleAsset.builder()
				.company(ticket.getCompany())
				.tangibleAssetItem(item)
				.assetCode(codeGenerator.generate(TANGIBLE_ASSET_CODE_PREFIX, TANGIBLE_ASSET_REDIS_KEY_PREFIX, companyId))
				.serialNumber(serialNumber)
				.location(result.getLocation())
				.purchaseDate(result.getPurchaseDate())
				.purchasePrice(unitPrice)
				.purchaseVendor(result.getPurchaseVendor())
				.warrantyExpiredAt(result.getWarrantyExpiredAt())
				.tangibleAssetStatus(TangibleAssetStatus.AVAILABLE)
				.build());

			TangibleAssetAssignment assignment = tangibleAssetAssignmentRepository.save(TangibleAssetAssignment.builder()
				.company(ticket.getCompany())
				.tangibleAsset(asset)
				.member(ticket.getRequester())
				.department(ticket.getDepartment())
				.assignmentType(UsageType.PERMANENT)
				.assignmentStatus(com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus.ACTIVE)
				.build());
			asset.markInUse(
				ticket.getRequester(),
				ticket.getDepartment(),
				UsageType.PERMANENT,
				resolveAssetUsageType(purchaseRequestTicket.getRequestedUsageType()),
				assignment.getAssignedAt(),
				null
			);
			assignedAssets.add(DirectPurchaseAssetAssignResponse.AssignedAssetResponse.of(
				asset.getId(),
				asset.getAssetCode(),
				assignment.getId(),
				serialNumber,
				null
			));
		}
		completeDirectPurchaseTicket(ticket, purchaseRequestTicket);

		return DirectPurchaseAssetAssignResponse.from(
			ticket,
			purchaseRequestTicket,
			result,
			AssetType.TANGIBLE,
			item.getId(),
			item.getProductName(),
			assignedAssets
		);
	}

	private DirectPurchaseAssetAssignResponse assignDirectPurchaseIntangibleAsset(
		UUID companyId,
		Ticket ticket,
		PurchaseRequestTicket purchaseRequestTicket,
		DirectPurchaseResult result,
		DirectPurchaseAssetAssignRequest request
	) {
		IntangibleAssetItem item = resolveDirectPurchaseIntangibleItem(companyId, purchaseRequestTicket, request);
		List<String> licenseCodes = resolveDirectPurchaseLicenseCodes(
			companyId,
			purchaseRequestTicket.getQuantity(),
			result,
			request
		);
		BigDecimal unitPrice = divideActualPrice(result.getActualPrice(), purchaseRequestTicket.getQuantity());
		List<DirectPurchaseAssetAssignResponse.AssignedAssetResponse> assignedAssets = new ArrayList<>();

		for (String licenseCode : licenseCodes) {
			IntangibleAsset asset = intangibleAssetRepository.save(IntangibleAsset.builder()
				.company(ticket.getCompany())
				.intangibleAssetItem(item)
				.assetCode(codeGenerator.generate(INTANGIBLE_ASSET_CODE_PREFIX, INTANGIBLE_ASSET_REDIS_KEY_PREFIX, companyId))
				.licenseCode(licenseCode)
				.seatCount(result.getSeatCount())
				.startedAt(result.getStartedAt())
				.expiredAt(result.getExpiredAt())
				.isAutoRenewal(result.getIsAutoRenewal())
				.billingCycle(result.getBillingCycle())
				.purchaseDate(result.getPurchaseDate())
				.purchasePrice(unitPrice)
				.purchaseVendor(result.getPurchaseVendor())
				.intangibleAssetStatus(IntangibleAssetStatus.AVAILABLE)
				.build());

			IntangibleAssetAssignment assignment = intangibleAssetAssignmentRepository.save(IntangibleAssetAssignment.builder()
				.company(ticket.getCompany())
				.intangibleAsset(asset)
				.member(ticket.getRequester())
				.department(ticket.getDepartment())
				.assignedAt(result.getStartedAt())
				.endedAt(result.getExpiredAt())
				.assignmentStatus(AssignmentStatus.ACTIVE)
				.build());
			if (asset.getSeatCount() == 1) {
				asset.assignTo(ticket.getRequester(), ticket.getDepartment());
			} else {
				asset.markInUse();
			}
			assignedAssets.add(DirectPurchaseAssetAssignResponse.AssignedAssetResponse.of(
				asset.getId(),
				asset.getAssetCode(),
				assignment.getId(),
				null,
				licenseCode
			));
		}
		completeDirectPurchaseTicket(ticket, purchaseRequestTicket);

		return DirectPurchaseAssetAssignResponse.from(
			ticket,
			purchaseRequestTicket,
			result,
			AssetType.INTANGIBLE,
			item.getId(),
			item.getProductName(),
			assignedAssets
		);
	}

	private TangibleAssetItem resolveDirectPurchaseTangibleItem(
		UUID companyId,
		PurchaseRequestTicket purchaseRequestTicket,
		DirectPurchaseAssetAssignRequest request
	) {
		if (Boolean.TRUE.equals(purchaseRequestTicket.getIsStandard())) {
			return purchaseRequestTicket.getTangibleAssetItem();
		}
		if (request.getItemId() != null) {
			TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
					request.getItemId(),
					companyId
				)
				.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));
			validateTangibleItemCategory(purchaseRequestTicket, item);
			return item;
		}

		String productName = normalize(request.getProductName());
		if (!StringUtils.hasText(productName)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Product name is required when creating a non-standard tangible item.");
		}
		if (tangibleAssetItemRepository.existsByCompany_IdAndProductName(companyId, productName)) {
			throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
		}

		return tangibleAssetItemRepository.save(TangibleAssetItem.builder()
			.company(purchaseRequestTicket.getCompany())
			.tangibleAssetCategory(purchaseRequestTicket.getTangibleAssetCategory())
			.productName(productName)
			.manufacturer(defaultText(request.getManufacturer(), purchaseRequestTicket.getManufacturer()))
			.modelName(normalize(request.getModelName()))
			.isStandard(Boolean.FALSE)
			.build());
	}

	private IntangibleAssetItem resolveDirectPurchaseIntangibleItem(
		UUID companyId,
		PurchaseRequestTicket purchaseRequestTicket,
		DirectPurchaseAssetAssignRequest request
	) {
		if (Boolean.TRUE.equals(purchaseRequestTicket.getIsStandard())) {
			return purchaseRequestTicket.getIntangibleAssetItem();
		}
		if (request.getItemId() != null) {
			IntangibleAssetItem item = intangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
					request.getItemId(),
					companyId
				)
				.orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));
			validateIntangibleItemCategoryAndLicenseType(purchaseRequestTicket, item);
			return item;
		}

		String productName = normalize(request.getProductName());
		if (!StringUtils.hasText(productName)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Product name is required when creating a non-standard intangible item.");
		}
		if (intangibleAssetItemRepository.existsByCompany_IdAndProductName(companyId, productName)) {
			throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
		}

		return intangibleAssetItemRepository.save(IntangibleAssetItem.builder()
			.company(purchaseRequestTicket.getCompany())
			.intangibleAssetCategory(purchaseRequestTicket.getIntangibleAssetCategory())
			.productName(productName)
			.provider(defaultText(request.getProvider(), purchaseRequestTicket.getManufacturer()))
			.licenseType(purchaseRequestTicket.getLicenseType())
			.isStandard(Boolean.FALSE)
			.build());
	}

	private void completeDirectPurchaseTicket(Ticket ticket, PurchaseRequestTicket purchaseRequestTicket) {
		purchaseRequestTicket.complete();
		ticket.changeProcessingStatus(TicketStatus.COMPLETED, LocalDateTime.now());
	}

	private List<String> resolveDirectPurchaseSerialNumbers(
		UUID companyId,
		UUID itemId,
		int quantity,
		DirectPurchaseResult result,
		DirectPurchaseAssetAssignRequest request
	) {
		List<String> serialNumbers = parseStoredValues(result.getSerialNumber());
		validateValueCount(serialNumbers, quantity, "Serial numbers must match the direct purchase quantity.");
		validateNoDuplicateValues(serialNumbers, "Duplicate serial numbers are not allowed.");
		for (String serialNumber : serialNumbers) {
			if (tangibleAssetRepository.existsByCompany_IdAndSerialNumberAndTangibleAssetItem_Id(
				companyId,
				serialNumber,
				itemId
			)) {
				throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_SERIAL_NUMBER);
			}
		}
		return serialNumbers;
	}

	private List<String> resolveDirectPurchaseLicenseCodes(
		UUID companyId,
		int quantity,
		DirectPurchaseResult result,
		DirectPurchaseAssetAssignRequest request
	) {
		List<String> licenseCodes = parseStoredValues(result.getLicenseCode());
		validateValueCount(licenseCodes, quantity, "License codes must match the direct purchase quantity.");
		validateNoDuplicateValues(licenseCodes, "Duplicate license codes are not allowed.");
		for (String licenseCode : licenseCodes) {
			if (intangibleAssetRepository.existsByCompany_IdAndLicenseCode(companyId, licenseCode)) {
				throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_LICENSE_CODE);
			}
		}
		return licenseCodes;
	}

	private List<String> normalizeValues(List<String> values) {
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		return values.stream()
			.map(this::normalize)
			.filter(StringUtils::hasText)
			.toList();
	}

	private String resolveDirectPurchaseSerialNumberStorage(
		UUID companyId,
		int quantity,
		DirectPurchaseResultCreateRequest request,
		DirectPurchaseResult existingResult
	) {
		List<String> serialNumbers = normalizeValues(request.getSerialNumbers());
		if (serialNumbers.isEmpty() && StringUtils.hasText(request.getSerialNumber())) {
			serialNumbers = List.of(normalize(request.getSerialNumber()));
		}
		validateValueCount(serialNumbers, quantity, "Serial numbers must match the direct purchase quantity.");
		validateNoDuplicateValues(serialNumbers, "Duplicate serial numbers are not allowed.");
		for (String serialNumber : serialNumbers) {
			if (isExistingStoredValue(existingResult == null ? null : existingResult.getSerialNumber(), serialNumber)) {
				continue;
			}
			if (tangibleAssetRepository.existsByCompany_IdAndSerialNumber(companyId, serialNumber)) {
				throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_SERIAL_NUMBER);
			}
		}
		return serializeValues(serialNumbers);
	}

	private String resolveDirectPurchaseLicenseCodeStorage(
		UUID companyId,
		int quantity,
		DirectPurchaseResultCreateRequest request,
		DirectPurchaseResult existingResult
	) {
		List<String> licenseCodes = normalizeValues(request.getLicenseCodes());
		if (licenseCodes.isEmpty() && StringUtils.hasText(request.getLicenseCode())) {
			licenseCodes = List.of(normalize(request.getLicenseCode()));
		}
		validateValueCount(licenseCodes, quantity, "License codes must match the direct purchase quantity.");
		validateNoDuplicateValues(licenseCodes, "Duplicate license codes are not allowed.");
		for (String licenseCode : licenseCodes) {
			if (isExistingStoredValue(existingResult == null ? null : existingResult.getLicenseCode(), licenseCode)) {
				continue;
			}
			if (intangibleAssetRepository.existsByCompany_IdAndLicenseCode(companyId, licenseCode)) {
				throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_LICENSE_CODE);
			}
		}
		return serializeValues(licenseCodes);
	}

	private boolean isExistingStoredValue(String storedValue, String value) {
		return parseStoredValues(storedValue).contains(value);
	}

	private String serializeValues(List<String> values) {
		try {
			return OBJECT_MAPPER.writeValueAsString(values);
		} catch (JsonProcessingException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Failed to serialize direct purchase asset identifiers.");
		}
	}

	private List<String> parseStoredValues(String storedValue) {
		String normalized = normalize(storedValue);
		if (!StringUtils.hasText(normalized)) {
			return List.of();
		}
		if (!normalized.startsWith("[")) {
			return List.of(normalized);
		}
		try {
			return OBJECT_MAPPER.readValue(normalized, STRING_LIST_TYPE).stream()
				.map(this::normalize)
				.filter(StringUtils::hasText)
				.toList();
		} catch (JsonProcessingException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Invalid direct purchase asset identifiers.");
		}
	}

	private void validateValueCount(List<String> values, int quantity, String message) {
		if (values.size() != quantity) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, message);
		}
	}

	private void validateNoDuplicateValues(List<String> values, String message) {
		Set<String> uniqueValues = new HashSet<>(values);
		if (uniqueValues.size() != values.size()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, message);
		}
	}

	private BigDecimal divideActualPrice(BigDecimal actualPrice, int quantity) {
		return actualPrice.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
	}

	private AssetUsageType resolveAssetUsageType(RequestedUsageType requestedUsageType) {
		return switch (requestedUsageType) {
			case PERSONAL -> AssetUsageType.PERSONAL;
			case DEPARTMENT -> AssetUsageType.DEPARTMENT;
		};
	}

	private PurchaseRequestTicketCreateResponse createPurchaseRequestTicket(
		AuthenticatedMember authenticatedMember,
		RequestMethod requestMethod,
		RequestedUsageType requestedUsageType,
		AssetType assetType,
		UUID categoryId,
		String requestedItemDetail,
		String manufacturer,
		LicenseType licenseType,
		String purchaseUrl,
		int quantity,
		BigDecimal expectedPrice,
		String requestReason
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Member approver = ticketApprovalResolver.resolveDepartmentApprover(requester);
		TangibleAssetCategory tangibleAssetCategory = null;
		IntangibleAssetCategory intangibleAssetCategory = null;

		if (assetType == AssetType.TANGIBLE) {
			validateTangiblePurchaseRequest(licenseType);
			tangibleAssetCategory = findTangibleAssetCategory(
				categoryId,
				companyId
			);
		} else {
			validateIntangiblePurchaseRequest(licenseType);
			intangibleAssetCategory = findIntangibleAssetCategory(
				categoryId,
				companyId
			);
		}

		Ticket ticket = ticketRepository.save(Ticket.createPurchaseRequest(
			requester.getCompany(),
			ticketNoGenerator.generate(companyId),
			requester,
			requester.getDepartment(),
			approver,
			normalize(requestReason)
		));

		PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository.save(
			PurchaseRequestTicket.create(
				ticket,
				requester.getCompany(),
				requestMethod,
				requestedUsageType,
				Boolean.FALSE,
				null,
				null,
				tangibleAssetCategory,
				intangibleAssetCategory,
				normalize(requestedItemDetail),
				normalize(manufacturer),
				licenseType,
				normalize(purchaseUrl),
				quantity,
				expectedPrice
			)
		);

		return PurchaseRequestTicketCreateResponse.from(
			ticket,
			purchaseRequestTicket,
			assetType,
			categoryId
		);
	}

	private PurchaseRequestTicketCreateResponse createPurchaseRequestTicket(
		AuthenticatedMember authenticatedMember,
		RequestMethod requestMethod,
		RequestedUsageType requestedUsageType,
		AssetType assetType,
		Boolean isStandard,
		TangibleAssetItem tangibleAssetItem,
		IntangibleAssetItem intangibleAssetItem,
		TangibleAssetCategory tangibleAssetCategory,
		IntangibleAssetCategory intangibleAssetCategory,
		String requestedItemDetail,
		String manufacturer,
		LicenseType licenseType,
		String purchaseUrl,
		int quantity,
		BigDecimal expectedPrice,
		String requestReason
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Member approver = ticketApprovalResolver.resolveDepartmentApprover(requester);

		Ticket ticket = ticketRepository.save(Ticket.createPurchaseRequest(
			requester.getCompany(),
			ticketNoGenerator.generate(companyId),
			requester,
			requester.getDepartment(),
			approver,
			normalize(requestReason)
		));

		PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository.save(
			PurchaseRequestTicket.create(
				ticket,
				requester.getCompany(),
				requestMethod,
				requestedUsageType,
				isStandard,
				tangibleAssetItem,
				intangibleAssetItem,
				tangibleAssetCategory,
				intangibleAssetCategory,
				normalize(requestedItemDetail),
				normalize(manufacturer),
				licenseType,
				normalize(purchaseUrl),
				quantity,
				expectedPrice
			)
		);

		return PurchaseRequestTicketCreateResponse.from(
			ticket,
			purchaseRequestTicket,
			assetType,
			resolveCategoryId(tangibleAssetCategory, intangibleAssetCategory)
		);
	}

	private TangibleAssetCategory findTangibleAssetCategory(UUID categoryId, UUID companyId) {
		TangibleAssetCategory category = tangibleAssetCategoryRepository.findById(categoryId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));

		if (!category.getCompany().getId().equals(companyId)) {
			throw new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND);
		}
		return category;
	}

	private IntangibleAssetCategory findIntangibleAssetCategory(UUID categoryId, UUID companyId) {
		IntangibleAssetCategory category = intangibleAssetCategoryRepository.findById(categoryId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND));

		if (!category.getCompany().getId().equals(companyId)) {
			throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND);
		}
		return category;
	}

	private DirectPurchaseTarget resolveDirectPurchaseTarget(
		DirectPurchaseRequestTicketCreateRequest request,
		UUID companyId
	) {
		if (Boolean.TRUE.equals(request.getIsStandard())) {
			validateStandardDirectPurchaseRequest(request);
			if (request.getAssetType() == AssetType.TANGIBLE) {
				validateTangiblePurchaseRequest(request.getLicenseType());
				TangibleAssetItem item = findStandardTangibleAssetItem(request.getAssetItemId(), companyId);
				return new DirectPurchaseTarget(
					item,
					null,
					item.getTangibleAssetCategory(),
					null,
					item.getProductName(),
					item.getManufacturer(),
					null
				);
			}

			IntangibleAssetItem item = findStandardIntangibleAssetItem(request.getAssetItemId(), companyId);
			if (request.getLicenseType() != null && request.getLicenseType() != item.getLicenseType()) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "License type must match the selected standard asset item.");
			}
			return new DirectPurchaseTarget(
				null,
				item,
				null,
				item.getIntangibleAssetCategory(),
				item.getProductName(),
				item.getProvider(),
				item.getLicenseType()
			);
		}

		validateNonStandardDirectPurchaseRequest(request);
		if (request.getAssetType() == AssetType.TANGIBLE) {
			validateTangiblePurchaseRequest(request.getLicenseType());
			return new DirectPurchaseTarget(
				null,
				null,
				findTangibleAssetCategory(request.getCategoryId(), companyId),
				null,
				normalize(request.getRequestedItemDetail()),
				normalize(request.getManufacturer()),
				null
			);
		}

		validateIntangiblePurchaseRequest(request.getLicenseType());
		return new DirectPurchaseTarget(
			null,
			null,
			null,
			findIntangibleAssetCategory(request.getCategoryId(), companyId),
			normalize(request.getRequestedItemDetail()),
			normalize(request.getManufacturer()),
			request.getLicenseType()
		);
	}

	private TangibleAssetItem findStandardTangibleAssetItem(UUID itemId, UUID companyId) {
		TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

		if (!item.getCompany().getId().equals(companyId) || !Boolean.TRUE.equals(item.getIsStandard())) {
			throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND);
		}
		return item;
	}

	private IntangibleAssetItem findStandardIntangibleAssetItem(UUID itemId, UUID companyId) {
		IntangibleAssetItem item = intangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));

		if (!item.getCompany().getId().equals(companyId) || !Boolean.TRUE.equals(item.getIsStandard())) {
			throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND);
		}
		return item;
	}

	private UUID resolveCategoryId(
		TangibleAssetCategory tangibleAssetCategory,
		IntangibleAssetCategory intangibleAssetCategory
	) {
		if (tangibleAssetCategory != null) {
			return tangibleAssetCategory.getId();
		}
		if (intangibleAssetCategory != null) {
			return intangibleAssetCategory.getId();
		}
		return null;
	}

	private void validateStandardDirectPurchaseRequest(DirectPurchaseRequestTicketCreateRequest request) {
		if (request.getAssetItemId() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Asset item ID is required for standard direct purchase requests.");
		}
	}

	private void validateNonStandardDirectPurchaseRequest(DirectPurchaseRequestTicketCreateRequest request) {
		if (request.getAssetItemId() != null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Asset item ID cannot be used for non-standard direct purchase requests.");
		}
		if (request.getCategoryId() == null
			|| !StringUtils.hasText(request.getRequestedItemDetail())
			|| !StringUtils.hasText(request.getManufacturer())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Category, item detail, and manufacturer are required for non-standard direct purchase requests.");
		}
	}

	private void validateTangiblePurchaseRequest(LicenseType licenseType) {
		if (licenseType != null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형 자산 요청에는 라이선스 유형을 입력할 수 없습니다.");
		}
	}

	private void validateIntangiblePurchaseRequest(LicenseType licenseType) {
		if (licenseType == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산 요청에는 라이선스 유형이 필수입니다.");
		}
	}

	private AssetType resolveAssetType(PurchaseRequestTicket purchaseRequestTicket) {
		if (purchaseRequestTicket.getTangibleAssetCategory() != null) {
			return AssetType.TANGIBLE;
		}
		return AssetType.INTANGIBLE;
	}

	private void validateDirectPurchaseResultTarget(
		PurchaseRequestTicket purchaseRequestTicket,
		Ticket ticket,
		Member submitter
	) {
		if (purchaseRequestTicket.getRequestMethod() != RequestMethod.DIRECT_PURCHASE) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "직접구매 티켓만 구매 완료 정보를 등록할 수 있습니다.");
		}
		if (!ticket.getRequester().getId().equals(submitter.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		if (ticket.getTicketStatus() != TicketStatus.IN_PROGRESS) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매자산팀 승인 이후에 구매 완료 정보를 등록할 수 있습니다.");
		}
		if (directPurchaseResultRepository.existsByPurchaseRequestTicket_Id(purchaseRequestTicket.getId())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 등록된 직접구매 완료 정보가 있습니다.");
		}
	}

	private void validateDirectPurchaseResultUpdatable(
		PurchaseRequestTicket purchaseRequestTicket,
		Ticket ticket,
		Member submitter
	) {
		if (purchaseRequestTicket.getRequestMethod() != RequestMethod.DIRECT_PURCHASE) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "직접구매 티켓만 구매 완료 정보를 수정할 수 있습니다.");
		}
		if (!ticket.getRequester().getId().equals(submitter.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		if (ticket.getTicketStatus() != TicketStatus.IN_PROGRESS) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구매자산팀 승인 이후에 구매 완료 정보를 수정할 수 있습니다.");
		}
	}

	private void validateDirectPurchaseResultReadable(Ticket ticket, Member member) {
		if (ticket.getRequester().getId().equals(member.getId())) {
			return;
		}

		MemberRole role = member.getRole();
		if (role == MemberRole.ADMIN || role == MemberRole.ASSET_MANAGER || role == MemberRole.ASSET_TEAM) {
			return;
		}

		throw new BusinessException(ErrorCode.ACCESS_DENIED);
	}

	private void validateDirectPurchaseAssetAssignee(Ticket ticket, Member member) {
		if (ticket.getAssignee() == null || !ticket.getAssignee().getId().equals(member.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateDirectPurchaseAssetAssignable(
		PurchaseRequestTicket purchaseRequestTicket,
		Ticket ticket,
		DirectPurchaseResult result
	) {
		if (purchaseRequestTicket.getRequestMethod() != RequestMethod.DIRECT_PURCHASE) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Only direct purchase tickets can register and assign direct purchase assets.");
		}
		if (ticket.getTicketStatus() != TicketStatus.IN_PROGRESS) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Direct purchase assets can be assigned only while the ticket is in progress.");
		}
		if (result.getConfirmationStatus() != ConfirmationStatus.CONFIRMED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Direct purchase result must be confirmed before asset registration.");
		}
		if (resolveAssetType(purchaseRequestTicket) == AssetType.TANGIBLE
			&& Boolean.TRUE.equals(purchaseRequestTicket.getIsStandard())
			&& purchaseRequestTicket.getTangibleAssetItem() == null) {
			throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND);
		}
		if (resolveAssetType(purchaseRequestTicket) == AssetType.INTANGIBLE
			&& Boolean.TRUE.equals(purchaseRequestTicket.getIsStandard())
			&& purchaseRequestTicket.getIntangibleAssetItem() == null) {
			throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND);
		}
	}

	private void validateTangibleItemCategory(PurchaseRequestTicket purchaseRequestTicket, TangibleAssetItem item) {
		if (!item.getTangibleAssetCategory().getId().equals(purchaseRequestTicket.getTangibleAssetCategory().getId())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Selected tangible item category does not match the direct purchase ticket.");
		}
	}

	private void validateIntangibleItemCategoryAndLicenseType(
		PurchaseRequestTicket purchaseRequestTicket,
		IntangibleAssetItem item
	) {
		if (!item.getIntangibleAssetCategory().getId().equals(purchaseRequestTicket.getIntangibleAssetCategory().getId())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Selected intangible item category does not match the direct purchase ticket.");
		}
		if (item.getLicenseType() != purchaseRequestTicket.getLicenseType()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Selected intangible item license type does not match the direct purchase ticket.");
		}
	}

	private void validateDirectPurchaseResultRequest(
		UUID companyId,
		AssetType assetType,
		int quantity,
		DirectPurchaseResultCreateRequest request,
		DirectPurchaseResult existingResult
	) {
		if (assetType == AssetType.TANGIBLE) {
			validateTangibleDirectPurchaseResult(companyId, quantity, request, existingResult);
			return;
		}
		validateIntangibleDirectPurchaseResult(companyId, quantity, request, existingResult);
	}

	private void validateTangibleDirectPurchaseResult(
		UUID companyId,
		int quantity,
		DirectPurchaseResultCreateRequest request,
		DirectPurchaseResult existingResult
	) {
		String serialNumber = normalize(request.getSerialNumber());
		String location = normalize(request.getLocation());
		if (quantity == 1 && !StringUtils.hasText(serialNumber) && normalizeValues(request.getSerialNumbers()).isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형자산은 시리얼번호가 필수입니다.");
		}
		if (!StringUtils.hasText(location)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형자산은 위치가 필수입니다.");
		}
		if (request.getWarrantyExpiredAt() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형자산은 보증 만료일시가 필수입니다.");
		}
		if (hasIntangibleOnlyFields(request)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형자산에는 무형자산 구매 정보를 입력할 수 없습니다.");
		}
	}

	private void validateIntangibleDirectPurchaseResult(
		UUID companyId,
		int quantity,
		DirectPurchaseResultCreateRequest request,
		DirectPurchaseResult existingResult
	) {
		String licenseCode = normalize(request.getLicenseCode());
		if (quantity == 1 && !StringUtils.hasText(licenseCode) && normalizeValues(request.getLicenseCodes()).isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산은 라이선스코드가 필수입니다.");
		}
		if (request.getSeatCount() == null || request.getSeatCount() < 1) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산은 좌석 수가 1 이상이어야 합니다.");
		}
		if (request.getIsAutoRenewal() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산은 자동 갱신 여부가 필수입니다.");
		}
		if (request.getStartedAt() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산은 사용 시작일시가 필수입니다.");
		}
		if (request.getExpiredAt() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산은 만료일시가 필수입니다.");
		}
		if (!request.getExpiredAt().isAfter(request.getStartedAt())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산 만료일시는 사용 시작일시보다 이후여야 합니다.");
		}
		if (request.getBillingCycle() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산은 결제주기가 필수입니다.");
		}
		if (hasTangibleOnlyFields(request)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산에는 유형자산 구매 정보를 입력할 수 없습니다.");
		}
	}

	private boolean hasTangibleOnlyFields(DirectPurchaseResultCreateRequest request) {
		return StringUtils.hasText(request.getSerialNumber())
			|| !normalizeValues(request.getSerialNumbers()).isEmpty()
			|| StringUtils.hasText(request.getLocation())
			|| request.getWarrantyExpiredAt() != null;
	}

	private boolean hasIntangibleOnlyFields(DirectPurchaseResultCreateRequest request) {
		return StringUtils.hasText(request.getLicenseCode())
			|| !normalizeValues(request.getLicenseCodes()).isEmpty()
			|| request.getSeatCount() != null
			|| request.getIsAutoRenewal() != null
			|| request.getStartedAt() != null
			|| request.getExpiredAt() != null
			|| request.getBillingCycle() != null;
	}

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}

	private String defaultText(String requestedValue, String fallbackValue) {
		String normalized = normalize(requestedValue);
		if (StringUtils.hasText(normalized)) {
			return normalized;
		}
		return normalize(fallbackValue);
	}

	private record DirectPurchaseTarget(
		TangibleAssetItem tangibleAssetItem,
		IntangibleAssetItem intangibleAssetItem,
		TangibleAssetCategory tangibleAssetCategory,
		IntangibleAssetCategory intangibleAssetCategory,
		String requestedItemDetail,
		String manufacturer,
		LicenseType licenseType
	) {
	}
}
