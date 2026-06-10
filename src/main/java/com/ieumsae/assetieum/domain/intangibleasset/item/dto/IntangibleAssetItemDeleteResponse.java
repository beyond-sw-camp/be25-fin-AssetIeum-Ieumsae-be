package com.ieumsae.assetieum.domain.intangibleasset.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class IntangibleAssetItemDeleteResponse {
    private UUID intangibleAssetItemId;

    private UUID companyId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;
}
