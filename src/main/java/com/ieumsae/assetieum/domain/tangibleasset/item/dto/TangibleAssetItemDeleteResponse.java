package com.ieumsae.assetieum.domain.tangibleasset.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TangibleAssetItemDeleteResponse {
    private UUID tangibleAssetItemId;

    private UUID companyId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;
}
