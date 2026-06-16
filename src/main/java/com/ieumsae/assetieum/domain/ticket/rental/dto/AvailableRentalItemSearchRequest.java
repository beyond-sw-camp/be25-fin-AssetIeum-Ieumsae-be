package com.ieumsae.assetieum.domain.ticket.rental.dto;

import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AvailableRentalItemSearchRequest extends PaginationRequest {

	private UUID categoryId;

	private String keyword;

	private Boolean isStandard;
}
