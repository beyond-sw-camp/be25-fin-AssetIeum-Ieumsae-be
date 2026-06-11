package com.ieumsae.assetieum.domain.intangibleasset.item.dto;

import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class IntangibleAssetItemSearchRequest extends PaginationRequest {

    private UUID categoryId;

    private String keyword;

    private Boolean isStandard;
}
