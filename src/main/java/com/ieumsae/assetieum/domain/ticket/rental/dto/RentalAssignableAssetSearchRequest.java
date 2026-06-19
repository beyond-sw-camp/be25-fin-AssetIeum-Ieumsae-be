package com.ieumsae.assetieum.domain.ticket.rental.dto;

import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RentalAssignableAssetSearchRequest extends PaginationRequest {

	private String keyword;
}
