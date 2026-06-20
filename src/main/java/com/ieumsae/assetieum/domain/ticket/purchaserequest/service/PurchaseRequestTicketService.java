package com.ieumsae.assetieum.domain.ticket.purchaserequest.service;

import com.ieumsae.assetieum.domain.budget.budget.service.BudgetExecutionService;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.type.LicenseType;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.purchase.purchaseplan.entity.PurchasePlanItem;
import com.ieumsae.assetieum.domain.purchase.purchaseplanitem.repository.PurchasePlanItemRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
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
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseRequestTicketService {

	private final TicketRepository ticketRepository;
	private final PurchaseRequestTicketRepository purchaseRequestTicketRepository;
	private final DirectPurchaseResultRepository directPurchaseResultRepository;
	private final TangibleAssetRepository tangibleAssetRepository;
	private final IntangibleAssetRepository intangibleAssetRepository;
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
		validateDirectPurchaseResultRequest(companyId, assetType, request, null);

		DirectPurchaseResult result = directPurchaseResultRepository.save(DirectPurchaseResult.create(
			purchaseRequestTicket,
			submitter,
			request.getActualPrice(),
			request.getPurchaseDate(),
			normalize(request.getPurchaseVendor()),
			normalize(request.getSerialNumber()),
			normalize(request.getLocation()),
			request.getWarrantyExpiredAt(),
			normalize(request.getLicenseCode()),
			request.getSeatCount(),
			request.getIsAutoRenewal(),
			request.getStartedAt(),
			request.getExpiredAt(),
			request.getBillingCycle()
		));
		budgetExecutionService.executeForDirectPurchaseResult(ticket, companyId, request.getActualPrice());
		purchaseRequestTicket.complete();
		ticket.changeProcessingStatus(TicketStatus.COMPLETED, java.time.LocalDateTime.now());

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
		validateDirectPurchaseResultRequest(companyId, assetType, request, result);

		result.update(
			request.getActualPrice(),
			request.getPurchaseDate(),
			normalize(request.getPurchaseVendor()),
			normalize(request.getSerialNumber()),
			normalize(request.getLocation()),
			request.getWarrantyExpiredAt(),
			normalize(request.getLicenseCode()),
			request.getSeatCount(),
			request.getIsAutoRenewal(),
			request.getStartedAt(),
			request.getExpiredAt(),
			request.getBillingCycle()
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

	private void validateDirectPurchaseResultRequest(
		UUID companyId,
		AssetType assetType,
		DirectPurchaseResultCreateRequest request,
		DirectPurchaseResult existingResult
	) {
		if (assetType == AssetType.TANGIBLE) {
			validateTangibleDirectPurchaseResult(companyId, request, existingResult);
			return;
		}
		validateIntangibleDirectPurchaseResult(companyId, request, existingResult);
	}

	private void validateTangibleDirectPurchaseResult(
		UUID companyId,
		DirectPurchaseResultCreateRequest request,
		DirectPurchaseResult existingResult
	) {
		String serialNumber = normalize(request.getSerialNumber());
		String location = normalize(request.getLocation());
		if (!StringUtils.hasText(serialNumber)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형자산은 시리얼번호가 필수입니다.");
		}
		if (!StringUtils.hasText(location)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형자산은 위치가 필수입니다.");
		}
		if (request.getWarrantyExpiredAt() == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형자산은 보증 만료일시가 필수입니다.");
		}
		boolean isNewSerialNumber = (existingResult == null) || !serialNumber.equals(existingResult.getSerialNumber());
		if (isNewSerialNumber && tangibleAssetRepository.existsByCompany_IdAndSerialNumber(companyId, serialNumber)) {
			throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_SERIAL_NUMBER);
		}
		if (hasIntangibleOnlyFields(request)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형자산에는 무형자산 구매 정보를 입력할 수 없습니다.");
		}
	}

	private void validateIntangibleDirectPurchaseResult(
		UUID companyId,
		DirectPurchaseResultCreateRequest request,
		DirectPurchaseResult existingResult
	) {
		String licenseCode = normalize(request.getLicenseCode());
		if (!StringUtils.hasText(licenseCode)) {
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
		boolean isNewLicenseCode = (existingResult == null) || !licenseCode.equals(existingResult.getLicenseCode());
		if (isNewLicenseCode && intangibleAssetRepository.existsByCompany_IdAndLicenseCode(companyId, licenseCode)) {
			throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_LICENSE_CODE);
		}
		if (hasTangibleOnlyFields(request)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산에는 유형자산 구매 정보를 입력할 수 없습니다.");
		}
	}

	private boolean hasTangibleOnlyFields(DirectPurchaseResultCreateRequest request) {
		return StringUtils.hasText(request.getSerialNumber())
			|| StringUtils.hasText(request.getLocation())
			|| request.getWarrantyExpiredAt() != null;
	}

	private boolean hasIntangibleOnlyFields(DirectPurchaseResultCreateRequest request) {
		return StringUtils.hasText(request.getLicenseCode())
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
