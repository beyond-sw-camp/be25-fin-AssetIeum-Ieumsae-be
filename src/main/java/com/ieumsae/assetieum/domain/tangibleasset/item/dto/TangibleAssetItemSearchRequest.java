package com.ieumsae.assetieum.domain.tangibleasset.item.dto;

import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TangibleAssetItemSearchRequest extends PaginationRequest {
    @NotNull
    private UUID companyId;

    private UUID categoryId;

    private String productName;

    private String manufacturer;

    private String modelName;

    private Boolean isStandard;
}
