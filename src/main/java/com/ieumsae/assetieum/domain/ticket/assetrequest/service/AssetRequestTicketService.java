package com.ieumsae.assetieum.domain.ticket.assetrequest.service;

import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignableItemSearchRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignableItemsResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketDetailResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.assetrequest.repository.AssetRequestTicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketApprovalResolver;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketRequesterResolver;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
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
	private final TangibleAssetItemRepository tangibleAssetItemRepository;
	private final IntangibleAssetItemRepository intangibleAssetItemRepository;
	private final TicketNoGenerator ticketNoGenerator;
	private final TicketApprovalResolver ticketApprovalResolver;
	private final TicketRequesterResolver ticketRequesterResolver;
	private final AssetRequestAssignmentService assetRequestAssignmentService;
	private final AssetRequestAvailabilityService assetRequestAvailabilityService;
	private final AssetRequestActionResolver assetRequestActionResolver;

	@Transactional
	public AssetRequestTicketCreateResponse createAssetRequestTicket(
		AuthenticatedMember authenticatedMember,
		AssetRequestTicketCreateRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member requester = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		Member approver = ticketApprovalResolver.resolveDepartmentApprover(requester);
		TangibleAssetItem tangibleAssetItem = null;
		IntangibleAssetItem intangibleAssetItem = null;

		if (request.getAssetType() == AssetType.TANGIBLE) {
			tangibleAssetItem = findTangibleAssetItem(request.getAssetItemId(), companyId);
		} else {
			intangibleAssetItem = findIntangibleAssetItem(
				request.getAssetItemId(),
				companyId
			);
		}

		Ticket ticket = ticketRepository.save(Ticket.createAssetRequest(
			requester.getCompany(),
			ticketNoGenerator.generate(companyId),
			requester,
			requester.getDepartment(),
			approver,
			normalize(request.getRequestReason())
		));

		AssetRequestTicket assetRequestTicket = assetRequestTicketRepository.save(
			AssetRequestTicket.createRequest(
				ticket,
				requester.getCompany(),
				request.getRequestedUsageType(),
				tangibleAssetItem,
				intangibleAssetItem,
				request.getQuantity()
			)
		);

		return AssetRequestTicketCreateResponse.from(
			ticket,
			assetRequestTicket,
			request.getAssetType(),
			request.getAssetItemId()
		);
	}

	public AssetRequestTicketDetailResponse getAssetRequestTicket(
		AuthenticatedMember authenticatedMember,
		UUID ticketId
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		AssetRequestTicket assetRequestTicket = findAssetRequestTicket(ticketId, companyId);
		Ticket ticket = assetRequestTicket.getTicket();

		assetRequestActionResolver.validateReadable(ticket, viewer);
		boolean requesterView = ticket.getRequester().getId().equals(viewer.getId());

		return AssetRequestTicketDetailResponse.from(
			ticket,
			assetRequestTicket,
			viewer.getRole(),
			requesterView,
			assetRequestActionResolver.createActions(ticket, viewer)
		);
	}

	public AssetRequestAssignableItemsResponse getAssignableItems(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		AssetRequestAssignableItemSearchRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member viewer = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		AssetRequestTicket assetRequestTicket = findAssetRequestTicket(ticketId, companyId);
		Ticket ticket = assetRequestTicket.getTicket();

		assetRequestActionResolver.validateReadable(ticket, viewer);
		return assetRequestAvailabilityService.getAssignableItems(companyId, assetRequestTicket, request);
	}

	@Transactional
	public AssetRequestAssignResponse assignAssetRequest(
		AuthenticatedMember authenticatedMember,
		UUID ticketId,
		AssetRequestAssignRequest request
	) {
		UUID companyId = authenticatedMember.companyId();
		Member assignee = ticketRequesterResolver.resolveActiveRequester(authenticatedMember.id(), companyId);
		AssetRequestTicket assetRequestTicket = findAssetRequestTicket(ticketId, companyId);

		return assetRequestAssignmentService.assign(companyId, assignee, assetRequestTicket, request);
	}

	private TangibleAssetItem findTangibleAssetItem(UUID itemId, UUID companyId) {
		TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

		if (!item.getCompany().getId().equals(companyId)) {
			throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND);
		}
		return item;
	}

	private IntangibleAssetItem findIntangibleAssetItem(UUID itemId, UUID companyId) {
		IntangibleAssetItem item = intangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));

		if (!item.getCompany().getId().equals(companyId)) {
			throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND);
		}
		return item;
	}

	private AssetRequestTicket findAssetRequestTicket(UUID ticketId, UUID companyId) {
		return assetRequestTicketRepository.findByIdAndCompany_IdAndDeletedAtIsNull(ticketId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
	}

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
