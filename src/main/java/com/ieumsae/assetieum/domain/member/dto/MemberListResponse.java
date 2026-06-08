package com.ieumsae.assetieum.domain.member.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class MemberListResponse {

	private final List<MemberListItemResponse> content;
	private final int page;
	private final int size;
	private final SortResponse sort;
	private final long totalElements;
	private final int totalPages;
	private final boolean first;
	private final boolean last;

	public static MemberListResponse from(Page<MemberListItemResponse> page) {
		return MemberListResponse.builder()
			.content(page.getContent())
			.page(page.getNumber())
			.size(page.getSize())
			.sort(SortResponse.createdAtDesc())
			.totalElements(page.getTotalElements())
			.totalPages(page.getTotalPages())
			.first(page.isFirst())
			.last(page.isLast())
			.build();
	}

	@Getter
	@Builder
	public static class SortResponse {
		private final String property;
		private final String direction;

		public static SortResponse createdAtDesc() {
			return SortResponse.builder()
				.property("createdAt")
				.direction("DESC")
				.build();
		}
	}
}
