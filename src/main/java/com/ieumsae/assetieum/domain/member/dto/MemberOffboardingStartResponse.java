package com.ieumsae.assetieum.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventStatus;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({
	"memberId",
	"memberName",
	"memberStatus",
	"returnedTangibleAssetCount",
	"endedIntangibleAssignmentCount",
	"cancelledTicketCount",
	"remainingTargetCount",
	"resignedAt",
	"reason",
	"hrEventId",
	"hrEventStatus"
})
public class MemberOffboardingStartResponse {

	private final UUID memberId;
	private final String memberName;
	private final MemberStatus memberStatus;
	private final long returnedTangibleAssetCount;
	private final long endedIntangibleAssignmentCount;
	private final long cancelledTicketCount;
	private final long remainingTargetCount;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime resignedAt;

	private final String reason;
	private final UUID hrEventId;
	private final HrEventStatus hrEventStatus;
}
