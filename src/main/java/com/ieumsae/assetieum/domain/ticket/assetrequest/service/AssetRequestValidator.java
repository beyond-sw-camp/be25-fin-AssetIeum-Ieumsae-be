package com.ieumsae.assetieum.domain.ticket.assetrequest.service;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestAssignRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.entity.AssetRequestTicket;
import com.ieumsae.assetieum.domain.ticket.assetrequest.type.AssetRequestTicketStatus;
import com.ieumsae.assetieum.domain.ticket.common.entity.Ticket;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.TicketStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssetRequestValidator {

	private final AssetRequestActionResolver actionResolver;

	public void validateAssignable(Ticket ticket, Member member) {
		if (!actionResolver.isAssetRole(member.getRole())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
		if (ticket.getTicketStatus() != TicketStatus.ASSET_APPROVED
			&& ticket.getTicketStatus() != TicketStatus.IN_PROGRESS) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자산 승인 상태의 자산 요청만 할당할 수 있습니다.");
		}
		if (ticket.getRequester().getId().equals(member.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	public void validateAssignmentTarget(
		AssetRequestTicket assetRequestTicket,
		AssetRequestAssignRequest request
	) {
		if (assetRequestTicket.getStatus() != AssetRequestTicketStatus.REQUESTED) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 할당 처리된 자산 요청입니다.");
		}
		boolean tangibleRequest = assetRequestTicket.getTangibleAssetItem() != null;
		if (tangibleRequest && request.getAssetType() != AssetType.TANGIBLE) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유형자산 요청에는 유형자산 품목만 할당할 수 있습니다.");
		}
		if (!tangibleRequest && request.getAssetType() != AssetType.INTANGIBLE) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "무형자산 요청에는 무형자산 품목만 할당할 수 있습니다.");
		}
	}
}
