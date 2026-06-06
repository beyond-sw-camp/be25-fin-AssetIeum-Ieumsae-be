package com.ieumsae.assetieum.domain.tangibleasset.item.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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

}
