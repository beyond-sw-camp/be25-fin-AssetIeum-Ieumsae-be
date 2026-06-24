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

    // 구매/요청 화면에서는 다른 부서 소유 seat을 제외한 할당 가능 수가 필요하다.
    private Boolean departmentScopedAvailableCount;
}
