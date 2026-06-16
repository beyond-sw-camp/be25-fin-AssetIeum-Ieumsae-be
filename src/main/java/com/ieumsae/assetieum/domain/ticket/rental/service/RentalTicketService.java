package com.ieumsae.assetieum.domain.ticket.rental.service;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketNoGenerator;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.rental.dto.RentalTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.rental.entity.RentalTicket;
import com.ieumsae.assetieum.domain.ticket.rental.repository.RentalTicketRepository;
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
public class RentalTicketService {

	private final TicketRepository ticketRepository;
	private final RentalTicketRepository rentalTicketRepository;
	private final MemberRepository memberRepository;
	private final TangibleAssetItemRepository tangibleAssetItemRepository;
	private final TicketNoGenerator ticketNoGenerator;

	@Transactional
	public RentalTicketCreateResponse createRentalTicket(
		AuthenticatedMember authenticatedMember,
		RentalTicketCreateRequest request
	) {
		validateRentalPeriod(request);

		UUID companyId = authenticatedMember.companyId();
		Member requester = findActiveRequester(authenticatedMember.id(), companyId);
		Member approver = findDepartmentManager(requester);
		TangibleAssetItem item = findTangibleAssetItem(request.getTangibleAssetItemId(), companyId);

		Ticket ticket = ticketRepository.save(Ticket.createRental(
			requester.getCompany(),
			ticketNoGenerator.generate(companyId),
			requester,
			requester.getDepartment(),
			approver,
			normalize(request.getRequestReason())
		));

		RentalTicket rentalTicket = rentalTicketRepository.save(RentalTicket.createRequest(
			ticket,
			requester.getCompany(),
			request.getRequestedUsageType(),
			item,
			request.getRentalStartDate(),
			request.getRequestedDueDate()
		));

		return RentalTicketCreateResponse.from(ticket, rentalTicket);
	}

	private void validateRentalPeriod(RentalTicketCreateRequest request) {
		if (!request.getRentalStartDate().isBefore(request.getRequestedDueDate())) {
			throw new BusinessException(
				ErrorCode.INVALID_RENTAL_PERIOD,
				"반납 예정 일시는 대여 시작 일시보다 이후여야 합니다."
			);
		}
	}

	private Member findActiveRequester(UUID memberId, UUID companyId) {
		Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (!member.isActive()) {
			throw new BusinessException(ErrorCode.INACTIVE_MEMBER);
		}
		return member;
	}

	private Member findDepartmentManager(Member requester) {
		Member departmentManager = requester.getDepartment().getDepartmentManager();
		if (departmentManager == null
			|| !departmentManager.isActive()
			|| departmentManager.getRole() != MemberRole.DEPARTMENT_MANAGER) {
			throw new BusinessException(ErrorCode.INVALID_DEPARTMENT_MANAGER, "부서장이 지정되지 않았거나 활성 상태가 아닙니다.");
		}
		return departmentManager;
	}

	private TangibleAssetItem findTangibleAssetItem(UUID itemId, UUID companyId) {
		TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

		if (!item.getCompany().getId().equals(companyId)) {
			throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND);
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
