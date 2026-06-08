package com.ieumsae.assetieum.domain.tangibleasset.item.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemSearchRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TangibleAssetItemService {

    private final TangibleAssetItemRepository tangibleAssetItemRepository;
    private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
    private final CompanyRepository companyRepository;

    /**
     * 유형자산 품목 등록.
     * 동일 회사 내 품목명, 모델명 중복 여부를 검증한다.
     */
    @Transactional
    public TangibleAssetItemResponse createItem(
            TangibleAssetItemCreateRequest request
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        TangibleAssetCategory category = tangibleAssetCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));

        if (!category.getCompany().getId().equals(request.getCompanyId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED_COMPANY_SCOPE);
        }

        if(tangibleAssetItemRepository.existsByCompany_IdAndProductName(
                request.getCompanyId(),
                request.getProductName()
        )){
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
        }

        if(tangibleAssetItemRepository.existsByCompany_IdAndModelName(
                request.getCompanyId(),
                request.getModelName()
        )) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_MODEL_NAME);
        }

        // 2. 품목 생성 및 저장
        TangibleAssetItem item = TangibleAssetItem.builder()
                .company(company)
                .tangibleAssetCategory(category)
                .productName(request.getProductName())
                .manufacturer(request.getManufacturer())
                .modelName(request.getModelName())
                .isStandard(request.getIsStandard() != null ? request.getIsStandard() : true)
                .build();

        TangibleAssetItem savedItem = tangibleAssetItemRepository.save(item);

        return TangibleAssetItemResponse.from(
                savedItem
        );
    }

    /**
     * 회사 기준 유형자산 품목 목록 조회.
     * 카테고리, 품목명, 제조사, 모델명, 표준 여부를 기준으로 필터링하여
     * 해당하는 품목만 조회하여 반환한다.
     */
    public PaginationResponse<TangibleAssetItemResponse> getItems(
            TangibleAssetItemSearchRequest request
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. 페이징 처리 및 필터링 후 품목 목록 반환
        Page<TangibleAssetItem> itemPage =
                tangibleAssetItemRepository.search(
                        request.getCompanyId(),
                        request.getCategoryId(),
                        request.getProductName(),
                        request.getManufacturer(),
                        request.getModelName(),
                        request.getIsStandard(),
                        request.toPageable()
                );

        Page<TangibleAssetItemResponse> responsePage =
                itemPage.map(TangibleAssetItemResponse::from);

        return PaginationResponse.from(responsePage);
    }
}
