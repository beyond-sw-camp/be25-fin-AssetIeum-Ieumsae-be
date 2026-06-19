package com.ieumsae.assetieum.domain.intangibleasset.item.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.intangibleasset.category.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.category.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemDeleteResponse;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemResponse;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemSearchRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.dto.IntangibleAssetItemUpdateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.item.entity.IntangibleAssetItem;
import com.ieumsae.assetieum.domain.intangibleasset.item.repository.IntangibleAssetItemRepository;
import com.ieumsae.assetieum.domain.intangibleasset.item.type.LicenseType;
import com.ieumsae.assetieum.global.common.csv.CsvFileReader;
import com.ieumsae.assetieum.global.common.csv.CsvValueParser;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntangibleAssetItemService {

    private final IntangibleAssetItemRepository intangibleAssetItemRepository;
    private final IntangibleAssetRepository intangibleAssetRepository;
    private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
    private final IntangibleAssetCategoryRepository intangibleAssetCategoryRepository;
    private final CompanyRepository companyRepository;
    private final CsvFileReader csvFileReader;

    /**
     * 무형자산 품목 삭제 (soft delete)
     * 해당 품목의 자산이 존재하는 경우,
     * 삭제를 제한한다.
     */
    @Transactional
    public IntangibleAssetItemDeleteResponse deleteItem(UUID itemId, UUID companyId) {
        // 1. 입력값 검증
        IntangibleAssetItem item = intangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(itemId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));

        if(intangibleAssetRepository.existsByCompany_IdAndIntangibleAssetItem_Id(
                companyId,
                item.getId()
        )) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_HAS_ASSETS);
        }

        // 2. 품목 삭제

        item.delete();

        return IntangibleAssetItemDeleteResponse.builder()
                .intangibleAssetItemId(item.getId())
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
            IntangibleAssetItemUpdateRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        IntangibleAssetItem item = intangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(itemId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_NOT_FOUND));

        IntangibleAssetCategory category = null;

        if(request.getCategoryId() != null) {
            category = intangibleAssetCategoryRepository.findByIdAndCompany_Id(request.getCategoryId(), companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND));
            validateLeafCategory(category, companyId);
        }

        if(intangibleAssetItemRepository.existsByCompany_IdAndProductName(
                companyId,
                request.getProductName()
        )) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_PRODUCT_NAME);
        }

        // 2. 품목 수정
        item.update(request, category);

        int availableSeatCount = calculateAvailableSeatCount(companyId, item.getId());
        return IntangibleAssetItemResponse.from(item, availableSeatCount);
    }

    /**
     * 무형자산 품목 등록
     * 동일 회사 내 품목명 중복 여부를 검증한다.
     */
    @Transactional
    public IntangibleAssetItemResponse createItem(
            IntangibleAssetItemCreateRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        IntangibleAssetCategory category = intangibleAssetCategoryRepository.findByIdAndCompany_Id(
                request.getCategoryId(),
                companyId
        )
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND));
        validateLeafCategory(category, companyId);

        if (intangibleAssetItemRepository.existsByCompany_IdAndProductName(
                companyId,
                request.getProductName()
        )) {
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

        return IntangibleAssetItemResponse.from(savedItem, 0);
    }

    /**
     * 회사 기준 무형자산 품목 목록 조회
     * 카테고리, 품목명, 제공사, 라이선스 유형, 표준 여부를 기준으로 필터링하여
     * 해당하는 품목만 조회하여 반환한다.
     */
    public PaginationResponse<IntangibleAssetItemResponse> getItems(
            IntangibleAssetItemSearchRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        // 2. 페이징 처리 및 필터링 후 품목 목록 반환
        Page<IntangibleAssetItemResponse> responsePage =
                intangibleAssetItemRepository.search(
                        companyId,
                        request.getCategoryId(),
                        request.getKeyword(),
                        request.getIsStandard(),
                        request.toPageable()
                );

        return PaginationResponse.from(responsePage);
    }

    @Transactional
    public List<IntangibleAssetItemResponse> importItems(
            MultipartFile file,
            UUID companyId
    ) {
        List<IntangibleAssetItemResponse> responses = new ArrayList<>();

        for (String[] columns : csvFileReader.readRows(file)) {
            if (columns.length != 5) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            IntangibleAssetCategory category = intangibleAssetCategoryRepository.findByCompany_IdAndName(
                            companyId,
                            columns[0].trim()
                    )
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND));
            validateLeafCategory(category, companyId);

            IntangibleAssetItemCreateRequest request = new IntangibleAssetItemCreateRequest(
                    category.getId(),
                    columns[1].trim(),
                    columns[2].trim(),
                    CsvValueParser.parseEnum(LicenseType.class, columns[3]),
                    CsvValueParser.parseBoolean(columns[4])
            );

            responses.add(createItem(request, companyId));
        }

        return responses;
    }

    private int calculateAvailableSeatCount(UUID companyId, UUID itemId) {
        List<IntangibleAsset> assets = intangibleAssetRepository
                .findAllByCompany_IdAndIntangibleAssetItem_IdAndIntangibleAssetStatusIn(
                        companyId,
                        itemId,
                        List.of(IntangibleAssetStatus.AVAILABLE, IntangibleAssetStatus.IN_USE)
                );

        int availableSeatCount = 0;
        for (IntangibleAsset asset : assets) {
            long activeAssignmentCount = intangibleAssetAssignmentRepository
                    .countByCompany_IdAndIntangibleAsset_IdAndAssignmentStatus(
                            companyId,
                            asset.getId(),
                            AssignmentStatus.ACTIVE
                    );
            availableSeatCount += Math.max(asset.getSeatCount() - Math.toIntExact(activeAssignmentCount), 0);
        }

        return availableSeatCount;
    }

    private void validateLeafCategory(IntangibleAssetCategory category, UUID companyId) {
        if (intangibleAssetCategoryRepository.existsByParent_IdAndCompany_Id(category.getId(), companyId)) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_CATEGORY_NOT_LEAF);
        }
    }
}
