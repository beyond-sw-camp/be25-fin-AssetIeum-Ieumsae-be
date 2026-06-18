package com.ieumsae.assetieum.domain.tangibleasset.item.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemSearchRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.dto.TangibleAssetItemUpdateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.global.common.csv.CsvFileReader;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TangibleAssetItemService {

    private final TangibleAssetItemRepository tangibleAssetItemRepository;
    private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
    private final CompanyRepository companyRepository;
    private final TangibleAssetRepository tangibleAssetRepository;
    private final CsvFileReader csvFileReader;

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

        validateLeafCategory(category, companyId);

        if (tangibleAssetItemRepository.existsByCompany_IdAndProductName(
                companyId,
                request.getProductName()
        )) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
        }

        if (tangibleAssetItemRepository.existsByCompany_IdAndModelName(
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
                savedItem,
                0
        );
    }

    /**
     * CSV 파일로 유형자산 품목을 일괄 등록한다.
     * 헤더 이후 각 행은 categoryName,productName,manufacturer,modelName,isStandard 순서여야 한다.
     */
    @Transactional
    public List<TangibleAssetItemResponse> importItems(
            MultipartFile file,
            UUID companyId
    ) {
        List<TangibleAssetItemResponse> responses = new ArrayList<>();

        for (String[] columns : csvFileReader.readRows(file)) {
            if (columns.length != 5) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            TangibleAssetCategory category = tangibleAssetCategoryRepository.findByCompany_IdAndName(
                            companyId,
                            columns[0].trim()
                    )
                    .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));

            validateLeafCategory(category, companyId);

            TangibleAssetItemCreateRequest request = new TangibleAssetItemCreateRequest(
                    category.getId(),
                    columns[1].trim(),
                    columns[2].trim(),
                    columns[3].trim(),
                    parseBoolean(columns[4].trim())
            );

            responses.add(createItem(request, companyId));
        }

        return responses;
    }

    /**
     * 회사 기준 유형자산 품목 목록 조회.
     */
    public PaginationResponse<TangibleAssetItemResponse> getItems(
            TangibleAssetItemSearchRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. 페이지 처리 및 필터링 된 품목 목록 반환
        Page<TangibleAssetItemResponse> responsePage =
                tangibleAssetItemRepository.search(
                        companyId,
                        request.getCategoryId(),
                        request.getKeyword(),
                        request.getIsStandard(),
                        request.toPageable()
                );

        return PaginationResponse.from(responsePage);
    }

    /**
     * 유형자산 품목 수정.
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

        if (request.getCategoryId() != null) {
            category = tangibleAssetCategoryRepository.findByIdAndCompany_Id(request.getCategoryId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));

            validateLeafCategory(category, companyId);
        }

        if (tangibleAssetItemRepository.existsByCompany_IdAndProductName(
                companyId,
                request.getProductName()
        )) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
        }

        if (tangibleAssetItemRepository.existsByCompany_IdAndModelName(
                companyId,
                request.getModelName()
        )) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_MODEL_NAME);
        }

        // 2. 품목 수정
        item.update(request, category);

        int availableAssetCount = Math.toIntExact(tangibleAssetRepository.countByCompany_IdAndTangibleAssetItem_IdAndTangibleAssetStatus(
                companyId,
                item.getId(),
                TangibleAssetStatus.AVAILABLE
        ));

        return TangibleAssetItemResponse.from(item, availableAssetCount);
    }

    /**
     * 유형자산 품목 삭제. (soft delete)
     */
    @Transactional
    public void deleteItem(UUID itemId, UUID companyId) {
        // 1. 입력값 검증
        TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(itemId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

        if (tangibleAssetRepository.existsByCompany_IdAndTangibleAssetItem_Id(
                companyId,
                item.getId()
        )) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_HAS_ASSETS);
        }

        // 2. 품목 삭제
        item.delete();
    }

    private void validateCsvFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        boolean hasCsvExtension = originalFilename != null && originalFilename.toLowerCase().endsWith(".csv");
        boolean hasCsvContentType = contentType != null
                && (contentType.equalsIgnoreCase("text/csv")
                || contentType.equalsIgnoreCase("application/csv")
                || contentType.equalsIgnoreCase("application/vnd.ms-excel"));

        if (file.isEmpty() || !hasCsvExtension || !hasCsvContentType) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private void validateLeafCategory(TangibleAssetCategory category, UUID companyId) {
        if (tangibleAssetCategoryRepository.existsByParent_IdAndCompany_Id(category.getId(), companyId)) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_CATEGORY_NOT_LEAF);
        }
    }
}
