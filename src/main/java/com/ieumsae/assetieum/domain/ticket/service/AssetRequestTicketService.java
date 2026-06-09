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
import com.ieumsae.assetieum.domain.ticket.type.AssetRequestTicketStatus;
import com.ieumsae.assetieum.domain.ticket.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.type.RequestMethod;
import com.ieumsae.assetieum.domain.ticket.type.TicketStatus;
import com.ieumsae.assetieum.domain.ticket.type.TicketType;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetRequestTicketService {

	private static final DateTimeFormatter TICKET_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final TicketRepository ticketRepository;
	private final AssetRequestTicketRepository assetRequestTicketRepository;
	private final MemberRepository memberRepository;
	private final TangibleAssetItemRepository tangibleAssetItemRepository;
	private final IntangibleAssetItemRepository intangibleAssetItemRepository;

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
			intangibleAssetItem = findStandardIntangibleAssetItem(request.getAssetItemId(), requester.getCompany().getId());
		}

		Ticket ticket = ticketRepository.save(Ticket.builder()
			.company(requester.getCompany())
			.ticketNo(createTicketNo())
			.ticketType(TicketType.ASSET_REQUEST)
			.ticketStatus(TicketStatus.REQUESTED)
			.requester(requester)
			.department(requester.getDepartment())
			.approver(approver)
			.requestReason(normalize(request.getRequestReason()))
			.build());

		assetRequestTicketRepository.save(AssetRequestTicket.builder()
			.ticket(ticket)
			.company(requester.getCompany())
			.status(AssetRequestTicketStatus.REQUESTED)
			.requestMethod(RequestMethod.TEAM_PURCHASE)
			.requestedUsageType(request.getRequestedUsageType())
			.quantity(request.getQuantity())
			.tangibleAssetItem(tangibleAssetItem)
			.intangibleAssetItem(intangibleAssetItem)
			.build());

		return StandardAssetRequestCreateResponse.from(ticket);
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
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "해당 무형 자산 품목이 존재하지 않습니다."));

		if (!item.getCompany().getId().equals(companyId) || !Boolean.TRUE.equals(item.getIsStandard())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "해당 무형 자산 품목이 존재하지 않습니다.");
		}
		return item;
	}

	private String createTicketNo() {
		String prefix = "TKT-" + LocalDate.now().format(TICKET_DATE_FORMAT) + "-";
		return prefix + String.format("%03d", findNextSequence(prefix));
	}

	private int findNextSequence(String prefix) {
		return ticketRepository.findTopByTicketNoStartingWithOrderByTicketNoDesc(prefix)
			.map(ticket -> ticket.getTicketNo().substring(prefix.length()))
			.map(Integer::parseInt)
			.map(sequence -> sequence + 1)
			.orElse(1);
	}

	private String normalize(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
