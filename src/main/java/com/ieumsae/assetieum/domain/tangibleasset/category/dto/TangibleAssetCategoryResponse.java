package com.ieumsae.assetieum.domain.tangibleasset.category.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({
        "categoryId",
        "companyId",
        "parentId",
        "name",
        "createdAt",
        "updatedAt"
})
public class TangibleAssetCategoryResponse {
    private UUID categoryId;

    private UUID companyId;

    private UUID parentId;

    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static TangibleAssetCategoryResponse from(
            TangibleAssetCategory category
    ) {
        return TangibleAssetCategoryResponse.builder()
                .categoryId(category.getId())
                .companyId(category.getCompany().getId())
                .parentId(
                        category.getParent() != null
                                ? category.getParent().getId()
                                : null
                )
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
