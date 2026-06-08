package com.ieumsae.assetieum.domain.member.dto;

import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberSearchRequest extends PaginationRequest {

	private String keyword;

	private UUID departmentId;

	private MemberStatus status;
}
