package com.ieumsae.assetieum.domain.ticket.service;

import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.dto.StandardAssetRequestCreateRequest;
import com.ieumsae.assetieum.domain.ticket.dto.StandardAssetRequestCreateResponse;
import com.ieumsae.assetieum.domain.ticket.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.repository.AssetRequestTicketRepository;
import com.ieumsae.assetieum.domain.ticket.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.type.AssetType;
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
public class AssetRequestTicketService {

	private final TicketRepository ticketRepository;
	private final AssetRequestTicketRepository assetRequestTicketRepository;
	private final MemberRepository memberRepository;
	private final TangibleAssetItemRepository tangibleAssetItemRepository;
	private final IntangibleAssetItemRepository intangibleAssetItemRepository;
	private final TicketNoGenerator ticketNoGenerator;

	@Transactional
	public StandardAssetRequestCreateResponse createStandardAssetRequest(
		AuthenticatedMember authenticatedMember,
		StandardAssetRequestCreateRequest request
	) {
		Member requester = findActiveRequester(authenticatedMember.id());
		Member approver = findDepartmentManager(requester);
		TangibleAssetItem tangibleAssetItem = null;
		IntangibleAssetItem intangibleAssetItem = null;

		if (request.getAssetType() == AssetType.TANGIBLE) {
			tangibleAssetItem = findStandardTangibleAssetItem(request.getAssetItemId(), requester.getCompany().getId());
		} else {
			intangibleAssetItem = findStandardIntangibleAssetItem(
				request.getAssetItemId(),
				requester.getCompany().getId()
			);
		}

		Ticket ticket = ticketRepository.save(Ticket.createAssetRequest(
			requester.getCompany(),
			ticketNoGenerator.generate(),
			requester,
			requester.getDepartment(),
			approver,
			normalize(request.getRequestReason())
		));

		assetRequestTicketRepository.save(
			AssetRequestTicket.createStandardRequest(
				ticket,
				requester.getCompany(),
				request.getRequestedUsageType(),
				tangibleAssetItem,
				intangibleAssetItem,
				request.getQuantity()
			)
		);

		return StandardAssetRequestCreateResponse.from(
			ticket,
			request.getRequestedUsageType(),
			request.getAssetType(),
			request.getAssetItemId(),
			request.getQuantity()
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

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
