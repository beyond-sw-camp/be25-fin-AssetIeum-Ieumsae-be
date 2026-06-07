package com.ieumsae.assetieum.domain.member.dto;

import com.ieumsae.assetieum.domain.member.entity.Member;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class MemberPageResponse {

	private final List<MemberListItemResponse> members;
	private final int pageNumber;
	private final int pageSize;
	private final long totalElements;
	private final int totalPages;
	private final boolean first;
	private final boolean last;

	public static MemberPageResponse from(Page<Member> page) {
		return MemberPageResponse.builder()
			.members(page.getContent().stream()
				.map(MemberListItemResponse::from)
				.toList())
			.pageNumber(page.getNumber())
			.pageSize(page.getSize())
			.totalElements(page.getTotalElements())
			.totalPages(page.getTotalPages())
			.first(page.isFirst())
			.last(page.isLast())
			.build();
	}
}
