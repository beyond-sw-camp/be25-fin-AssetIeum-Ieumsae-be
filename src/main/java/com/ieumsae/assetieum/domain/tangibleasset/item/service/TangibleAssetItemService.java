package com.ieumsae.assetieum.domain.tangibleasset.item.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemSearchRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemUpdateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TangibleAssetItemService {

    private final TangibleAssetItemRepository tangibleAssetItemRepository;
    private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
    private final CompanyRepository companyRepository;
    private final TangibleAssetRepository tangibleAssetRepository;

    /**
     * 유형자산 품목 등록.
     * 동일 회사 내 품목명, 모델명 중복 여부를 검증한다.
     */
    @Transactional
    public TangibleAssetItemResponse createItem(
            TangibleAssetItemCreateRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        TangibleAssetCategory category = tangibleAssetCategoryRepository.findByIdAndCompany_Id(
                        request.getCategoryId(),
                        companyId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));

        if(tangibleAssetItemRepository.existsByCompany_IdAndProductName(
                companyId,
                request.getProductName()
        )){
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
        }

        if(tangibleAssetItemRepository.existsByCompany_IdAndModelName(
                companyId,
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
            TangibleAssetItemSearchRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. 페이징 처리 및 필터링 후 품목 목록 반환
        Page<TangibleAssetItem> itemPage =
                tangibleAssetItemRepository.search(
                        companyId,
                        request.getCategoryId(),
                        request.getKeyword(),
                        request.getIsStandard(),
                        request.toPageable()
                );

        Page<TangibleAssetItemResponse> responsePage =
                itemPage.map(TangibleAssetItemResponse::from);

        return PaginationResponse.from(responsePage);
    }

    /**
     * 회사 기준 유형자산 품목 수정.
     * 카테고리, 품목명, 제조사, 모델명, 표준 여부을 수정하여
     * 해당하는 품목의 수정된 데이터를 반환한다.
     */
    @Transactional
    public TangibleAssetItemResponse updateItem(
            UUID itemId,
            TangibleAssetItemUpdateRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(itemId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

        TangibleAssetCategory category = null;

        if(request.getCategoryId() != null) {
            category = tangibleAssetCategoryRepository.findByIdAndCompany_Id(request.getCategoryId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));
        }

        if(tangibleAssetItemRepository.existsByCompany_IdAndProductName(
                companyId,
                request.getProductName()
        )){
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
        }

        if(tangibleAssetItemRepository.existsByCompany_IdAndModelName(
                companyId,
                request.getModelName()
        )) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_MODEL_NAME);
        }

        // 2. 품목 수정
        item.update(request, category);

        return TangibleAssetItemResponse.from(item);
    }

    /**
     * 유형자산 품목 삭제. (soft delete)
     * 해당 품목의 자산이 존재하는 경우,
     * 삭제를 제한한다.
     */
    @Transactional
    public void deleteItem(UUID itemId, UUID companyId) {
        // 1. 입력값 검증
        TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(itemId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

        if(tangibleAssetRepository.existsByCompany_IdAndTangibleAssetItem_Id(
                companyId,
                item.getId()
        )){
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_HAS_ASSETS);
        }

        // 2. 품목 삭제
        item.delete();

    }
}
