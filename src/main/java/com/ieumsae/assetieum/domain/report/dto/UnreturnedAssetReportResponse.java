package com.ieumsae.assetieum.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"totalUnreturnedAssetCount",
	"overdueReturnCount",
	"departmentUnreturnedAssets",
	"repeatDelayedUserCount",
	"repeatDelayedUserRate",
	"topDelayedUsers"
})
public class UnreturnedAssetReportResponse {

	private final long totalUnreturnedAssetCount;
	private final long overdueReturnCount;
	private final List<DepartmentUnreturnedAssetSummary> departmentUnreturnedAssets;
	private final long repeatDelayedUserCount;
	private final BigDecimal repeatDelayedUserRate;
	private final List<DelayedUserSummary> topDelayedUsers;

	@Getter
	@Builder
	@JsonPropertyOrder({
		"departmentId",
		"departmentName",
		"unreturnedAssetCount",
		"overdueReturnCount"
	})
	public static class DepartmentUnreturnedAssetSummary {
		private final UUID departmentId;
		private final String departmentName;
		private final long unreturnedAssetCount;
		private final long overdueReturnCount;
	}

	@Getter
	@Builder
	@JsonPropertyOrder({
		"rank",
		"memberId",
		"memberName",
		"departmentName",
		"delayCount",
		"averageDelayDays",
		"recentDelayedAt"
	})
	public static class DelayedUserSummary {
		private final int rank;
		private final UUID memberId;
		private final String memberName;
		private final String departmentName;
		private final long delayCount;
		private final BigDecimal averageDelayDays;

		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private final LocalDateTime recentDelayedAt;
	}
}
