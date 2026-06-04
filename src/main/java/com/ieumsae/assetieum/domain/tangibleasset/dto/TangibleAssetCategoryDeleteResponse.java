package com.ieumsae.assetieum.domain.tangibleasset.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TangibleAssetCategoryDeleteResponse {
    private UUID categoryId;

    private UUID companyId;
}
