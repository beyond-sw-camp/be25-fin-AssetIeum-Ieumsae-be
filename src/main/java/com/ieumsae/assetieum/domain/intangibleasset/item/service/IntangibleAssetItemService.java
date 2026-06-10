package com.ieumsae.assetieum.domain.intangibleasset.item.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemDeleteResponse;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemSearchRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemUpdateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntangibleAssetItemService {

    private final IntangibleAssetItemRepository intangibleAssetItemRepository;
    private final IntangibleAssetRepository intangibleAssetRepository;
    private final IntangibleAssetCategoryRepository intangibleAssetCategoryRepository;
    private final CompanyRepository companyRepository;

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

    /**
     * 회사 기준 무형자산 품목 수정.
     * 카테고리, 품목명, 제공사, 라이선스 유형, 표준 여부을 수정하여
     * 해당하는 품목의 수정된 데이터를 반환한다.
     */
    @Transactional
    public IntangibleAssetItemResponse updateItem(
            UUID itemId,
            IntangibleAssetItemUpdateRequest request
    ) {
        // 1. 입력값 검증
        IntangibleAssetItem item = intangibleAssetItemRepository.findByIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));

        IntangibleAssetCategory category = null;

        if(request.getCategoryId() != null) {
            category = intangibleAssetCategoryRepository.findById(item.getIntangibleAssetCategory().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND));
        }

        if(intangibleAssetItemRepository.existsByCompany_IdAndProductName(
                item.getCompany().getId(),
                request.getProductName()
        )) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
        }

        // 2. 품목 수정
        item.update(request, category);

        return IntangibleAssetItemResponse.from(item);
    }

    /**
     * 무형자산 품목 등록
     * 동일 회사 내 품목명 중복 여부를 검증한다.
     */
    @Transactional
    public IntangibleAssetItemResponse createItem(
            IntangibleAssetItemCreateRequest request
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        IntangibleAssetCategory category = intangibleAssetCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND));

        if (!category.getCompany().getId().equals(request.getCompanyId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED_COMPANY_SCOPE);
        }

        if(intangibleAssetItemRepository.existsByCompany_IdAndProductName(
                request.getCompanyId(),
                request.getProductName()
        )){
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
        }

        // 2. 품목 생성 및 저장
        IntangibleAssetItem item = IntangibleAssetItem.builder()
                .company(company)
                .intangibleAssetCategory(category)
                .productName(request.getProductName())
                .provider(request.getProvider())
                .licenseType(request.getLicenseType())
                .isStandard(request.getIsStandard())
                .build();

        IntangibleAssetItem savedItem = intangibleAssetItemRepository.save(item);

        return IntangibleAssetItemResponse.from(savedItem);
    }

    /**
     * 회사 기준 무형자산 품목 목록 조회
     * 카테고리, 품목명, 제공사, 라이선스 유형, 표준 여부를 기준으로 필터링하여
     * 해당하는 품목만 조회하여 반환한다.
     */
    public PaginationResponse<IntangibleAssetItemResponse> getItems(
            IntangibleAssetItemSearchRequest request
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. 페이징 처리 및 필터링 후 품목 목록 반환
        Page<IntangibleAssetItem> itemPage =
                intangibleAssetItemRepository.search(
                        request.getCompanyId(),
                        request.getCategoryId(),
                        request.getKeyword(),
                        request.getIsStandard(),
                        request.toPageable()
                );

        Page<IntangibleAssetItemResponse> responsePage =
                itemPage.map(IntangibleAssetItemResponse::from);

        return PaginationResponse.from(responsePage);
    }
}
