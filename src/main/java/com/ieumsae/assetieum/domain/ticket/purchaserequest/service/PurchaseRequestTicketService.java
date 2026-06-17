package com.ieumsae.assetieum.domain.ticket.purchaserequest.service;

import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.type.LicenseType;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketRequesterResolver;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.DirectPurchaseRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
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
	private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
	private final IntangibleAssetCategoryRepository intangibleAssetCategoryRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TicketRequesterResolver ticketRequesterResolver;

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
		return createPurchaseRequestTicket(
			authenticatedMember,
			RequestMethod.DIRECT_PURCHASE,
			request.getRequestedUsageType(),
			request.getAssetType(),
			request.getCategoryId(),
			request.getRequestedItemDetail(),
			request.getManufacturer(),
			request.getLicenseType(),
			null,
			request.getQuantity(),
			request.getExpectedPrice(),
			request.getRequestReason()
		);
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

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
