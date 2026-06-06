package com.ieumsae.assetieum.domain.intangibleasset.category.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class IntangibleAssetCategoryDeleteResponse {
    private UUID categoryId;

    private UUID companyId;
}
