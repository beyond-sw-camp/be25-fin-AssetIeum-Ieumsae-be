package com.ieumsae.assetieum.domain.intangibleasset.asset.dto;

import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class IntangibleAssetSearchRequest extends PaginationRequest {

    private UUID categoryId;

    private IntangibleAssetStatus status;

    private String keyword;

    private UUID currentUserId;

    private UUID departmentId;

}
