package com.ieumsae.assetieum.domain.tangibleasset.asset.dto;

import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TangibleAssetSearchRequest extends PaginationRequest {

    private UUID categoryId;

    private TangibleAssetStatus status;

    private String keyword;

    private UUID currentUserId;

    private UUID departmentId;

}