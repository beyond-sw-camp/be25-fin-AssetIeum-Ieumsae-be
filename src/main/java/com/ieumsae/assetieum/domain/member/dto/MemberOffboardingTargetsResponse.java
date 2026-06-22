package com.ieumsae.assetieum.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"memberId",
	"memberName",
	"departmentId",
	"departmentName",
	"memberStatus",
	"tangibleAssets",
	"intangibleAssets",
	"remainingTargetCount"
})
public class MemberOffboardingTargetsResponse {

	private final UUID memberId;
	private final String memberName;
	private final UUID departmentId;
	private final String departmentName;
	private final MemberStatus memberStatus;
	private final List<TangibleAssetTarget> tangibleAssets;
	private final List<IntangibleAssetTarget> intangibleAssets;
	private final long remainingTargetCount;

	@Getter
	@Builder
	public static class TangibleAssetTarget {
		private final UUID assetId;
		private final String assetCode;
		private final String assetName;
		private final TangibleAssetStatus assetStatus;

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private final LocalDateTime returnDueDate;
	}

	@Getter
	@Builder
	public static class IntangibleAssetTarget {
		private final UUID assetId;
		private final String assetCode;
		private final String assetName;
		private final IntangibleAssetStatus assetStatus;

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private final LocalDateTime expiredAt;
	}
}
