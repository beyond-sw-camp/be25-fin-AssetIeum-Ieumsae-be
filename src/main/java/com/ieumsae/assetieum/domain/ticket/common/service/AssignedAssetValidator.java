package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class AssignedAssetValidator {

	public boolean isTangibleInUseByAssignee(TangibleAssetAssignment assignment) {
		TangibleAsset asset = assignment.getTangibleAsset();

		return asset.getTangibleAssetStatus() == TangibleAssetStatus.IN_USE
			&& asset.getMember() != null
			&& asset.getMember().getId().equals(assignment.getMember().getId())
			&& asset.getCompany().getId().equals(assignment.getCompany().getId());
	}

	public boolean isIntangibleInUseByAssignee(IntangibleAssetAssignment assignment) {
		IntangibleAsset asset = assignment.getIntangibleAsset();

		return asset.getIntangibleAssetStatus() == IntangibleAssetStatus.IN_USE
			&& asset.getMember() != null
			&& asset.getMember().getId().equals(assignment.getMember().getId())
			&& asset.getCompany().getId().equals(assignment.getCompany().getId());
	}

	public void validateTangibleRequester(TangibleAssetAssignment assignment, Member requester) {
		TangibleAsset asset = assignment.getTangibleAsset();

		if (asset.getMember() == null || !asset.getMember().getId().equals(requester.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	public void validateIntangibleRequester(IntangibleAssetAssignment assignment, Member requester) {
		IntangibleAsset asset = assignment.getIntangibleAsset();

		if (asset.getMember() == null || !asset.getMember().getId().equals(requester.getId())) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}
}
