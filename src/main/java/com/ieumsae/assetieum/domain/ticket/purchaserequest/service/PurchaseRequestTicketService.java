package com.ieumsae.assetieum.domain.ticket.purchaserequest.service;

import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.dto.PurchaseRequestTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.entity.PurchaseRequestTicket;
import com.ieumsae.assetieum.domain.ticket.purchaserequest.repository.PurchaseRequestTicketRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
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
	private final MemberRepository memberRepository;
	private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
	private final IntangibleAssetCategoryRepository intangibleAssetCategoryRepository;
	private final TicketNoGenerator ticketNoGenerator;

	@Transactional
	public PurchaseRequestTicketCreateResponse createPurchaseRequestTicket(
		AuthenticatedMember authenticatedMember,
		PurchaseRequestTicketCreateRequest request
	) {
		Member requester = findActiveRequester(authenticatedMember.id());
		Member approver = findDepartmentManager(requester);
		TangibleAssetCategory tangibleAssetCategory = null;
		IntangibleAssetCategory intangibleAssetCategory = null;

		if (request.getAssetType() == AssetType.TANGIBLE) {
			validateTangiblePurchaseRequest(request);
			tangibleAssetCategory = findTangibleAssetCategory(
				request.getCategoryId(),
				requester.getCompany().getId()
			);
		} else {
			intangibleAssetCategory = findIntangibleAssetCategory(
				request.getCategoryId(),
				requester.getCompany().getId()
			);
		}

		Ticket ticket = ticketRepository.save(Ticket.createPurchaseRequest(
			requester.getCompany(),
			ticketNoGenerator.generate(),
			requester,
			requester.getDepartment(),
			approver,
			normalize(request.getRequestReason())
		));

		PurchaseRequestTicket purchaseRequestTicket = purchaseRequestTicketRepository.save(
			PurchaseRequestTicket.create(
				ticket,
				requester.getCompany(),
				request.getRequestMethod(),
				request.getRequestedUsageType(),
				tangibleAssetCategory,
				intangibleAssetCategory,
				normalize(request.getRequestedItemDetail()),
				normalize(request.getManufacturer()),
				request.getLicenseType(),
				normalize(request.getPurchaseUrl()),
				request.getQuantity(),
				request.getExpectedPrice()
			)
		);

		return PurchaseRequestTicketCreateResponse.from(
			ticket,
			purchaseRequestTicket,
			request.getAssetType(),
			request.getCategoryId()
		);
	}

	private Member findActiveRequester(UUID memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}
		return member;
	}

	private Member findDepartmentManager(Member requester) {
		Member departmentManager = requester.getDepartment().getDepartmentManager();
		if (departmentManager == null || !departmentManager.isActive()) {
			throw new BusinessException(ErrorCode.INVALID_DEPARTMENT_MANAGER, "부서장이 지정되지 않았거나 활성 상태가 아닙니다.");
		}
		return departmentManager;
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

	private void validateTangiblePurchaseRequest(PurchaseRequestTicketCreateRequest request) {
		if (request.getLicenseType() != null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형 자산 요청에는 라이선스 유형을 입력할 수 없습니다.");
		}
	}

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
