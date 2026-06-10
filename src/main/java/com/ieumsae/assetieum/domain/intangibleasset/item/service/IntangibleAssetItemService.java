package com.ieumsae.assetieum.domain.intangibleasset.item.service;

import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemDeleteResponse;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntangibleAssetItemService {

    private final IntangibleAssetItemRepository intangibleAssetItemRepository;
    private final IntangibleAssetRepository intangibleAssetRepository;

    /**
     * 무형자산 품목 삭제 (soft delete)
     * 해당 품목의 자산이 존재하는 경우,
     * 삭제를 제한한다.
     */
    @Transactional
    public IntangibleAssetItemDeleteResponse deleteItem(UUID itemId) {
        // 1. 입력값 검증
        IntangibleAssetItem item = intangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));

        if(intangibleAssetRepository.existsByCompany_IdAndIntangibleAssetItem_Id(
                item.getCompany().getId(),
                item.getId()
        )) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_HAS_ASSETS);
        }

        // 2. 품목 삭제

        item.delete();

        return IntangibleAssetItemDeleteResponse.builder()
                .intangibleAssetItemId(item.getId())
                .companyId(item.getCompany().getId())
                .deletedAt(item.getDeletedAt())
                .build();

    }
}
