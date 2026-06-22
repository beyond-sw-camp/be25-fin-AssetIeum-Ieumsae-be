package com.ieumsae.assetieum.domain.log.dto;

import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import java.util.UUID;

final class LogTargetPathResolver {

	private static final UUID EMPTY_UUID = new UUID(0L, 0L);

	private LogTargetPathResolver() {
	}

	static String resolve(LogSubjectType subjectType, UUID subjectId) {
		if (subjectType == null || subjectId == null || EMPTY_UUID.equals(subjectId)) {
			return null;
		}
		return switch (subjectType) {
			case COMPANY -> "/companies/" + subjectId;
			case DEPARTMENT -> "/departments/" + subjectId;
			case MEMBER -> "/members/" + subjectId;
			case TANGIBLE_ASSET -> "/tangible-asset/assets/" + subjectId;
			case INTANGIBLE_ASSET -> "/intangible-asset/assets/" + subjectId;
			case TANGIBLE_ASSET_ITEM -> "/tangible-asset/items/" + subjectId;
			case INTANGIBLE_ASSET_ITEM -> "/intangible-asset/items/" + subjectId;
			case TICKET -> "/tickets/" + subjectId;
			case PURCHASE_PLAN -> "/purchase-plans/" + subjectId;
			case BUDGET -> "/budgets/" + subjectId;
			case HR_EVENT -> "/hr-events/" + subjectId;
			case INSPECTION -> "/inspections/" + subjectId;
			case SYSTEM -> null;
		};
	}
}
