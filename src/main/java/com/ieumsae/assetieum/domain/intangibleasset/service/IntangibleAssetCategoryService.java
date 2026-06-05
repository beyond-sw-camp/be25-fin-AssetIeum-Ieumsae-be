package com.ieumsae.assetieum.domain.intangibleasset.service;

import com.ieumsae.assetieum.domain.company.Company;
import com.ieumsae.assetieum.domain.company.CompanyRepository;
import com.ieumsae.assetieum.domain.intangibleasset.dto.IntangibleAssetCategoryCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.dto.IntangibleAssetCategoryDeleteResponse;
import com.ieumsae.assetieum.domain.intangibleasset.dto.IntangibleAssetCategoryResponse;
import com.ieumsae.assetieum.domain.intangibleasset.dto.IntangibleAssetCategoryTreeResponse;
import com.ieumsae.assetieum.domain.intangibleasset.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntangibleAssetCategoryService {

    private final IntangibleAssetCategoryRepository intangibleAssetCategoryRepository;
    private final CompanyRepository companyRepository;

    /**
     * 무형자산 카테고리 등록.
     * 동일 회사 내 카테고리명 중복 여부를 검증하고,
     * parentId가 존재할 경우 부모 카테고리 유효성 및
     * 동일 회사 소속 여부를 함께 검증한다.
     */
    @Transactional
    public IntangibleAssetCategoryResponse createCategory(
            IntangibleAssetCategoryCreateRequest request
    ) {
        // 1. 입력값 검증
        if(intangibleAssetCategoryRepository.existsByCompany_IdAndName(
                request.getCompanyId(),
                request.getName()
        )) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_ALREADY_EXISTS);
        }

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        IntangibleAssetCategory parent = null;
        if(request.getParentId() != null){
            parent = intangibleAssetCategoryRepository.findById(
                    request.getParentId()
            ).orElseThrow(() ->
                    new BusinessException(
                            ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND
                    ));

            if(!parent.getCompany().getId().equals(request.getCompanyId())) {
                throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_INVALID_PARENT);
            }
        }

        // 2. 카테고리 생성 및 저장
        IntangibleAssetCategory category = IntangibleAssetCategory.builder()
                .company(company)
                .parent(parent)
                .name(request.getName())
                .build();

        IntangibleAssetCategory savedCategory = intangibleAssetCategoryRepository.save(category);

        return IntangibleAssetCategoryResponse.from(
                savedCategory
        );
    }

    /**
     * 회사 기준 무형자산 카테고리 목록 조회.
     * 전체 카테고리를 조회한 뒤 parent 관계를 기준으로
     * children을 연결하여 트리 구조로 반환한다.
     */
    public List<IntangibleAssetCategoryTreeResponse> getIntangibleCategories(
            UUID companyId
    ) {
        // 1. 입력값 검증
        if(!companyRepository.existsById(companyId)) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }

        // 2. 회사에 속한 전체 카테고리 조회
        List<IntangibleAssetCategory> categories =
                intangibleAssetCategoryRepository.findAllByCompany_IdOrderByCreatedAtAsc(
                        companyId
                );

        Map<UUID, IntangibleAssetCategoryTreeResponse> categoryMap =
                new LinkedHashMap<>();

        // roots: parent가 없는 최상위 카테고리 목록
        List<IntangibleAssetCategoryTreeResponse> roots =
                new ArrayList<>();

        for(IntangibleAssetCategory category : categories) {
            categoryMap.put(
                    category.getId(),
                    IntangibleAssetCategoryTreeResponse.from(category)
            );
        }

        // 3. 각 카테고리의 parent 정보를 기준으로 트리 구조 구성
        for(IntangibleAssetCategory category : categories) {
            IntangibleAssetCategoryTreeResponse response =
                    categoryMap.get(category.getId());

            if(category.getParent() == null){
                roots.add(response);
                continue;
            }

            IntangibleAssetCategoryTreeResponse parent =
                    categoryMap.get(category.getParent().getId());

            if(parent != null) {
                parent.addChild(response);
            }
        }

        return roots;
    }

    /**
     * 무형자산 카테고리 삭제.
     * 하위 카테고리가 존재하는 경우 삭제를 제한한다.
     */
    @Transactional
    public IntangibleAssetCategoryDeleteResponse deleteCategory(UUID categoryId) {
        // 1. 입력값 검증
        IntangibleAssetCategory category =
                intangibleAssetCategoryRepository.findById(categoryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND));

        if(intangibleAssetCategoryRepository.existsByParent_Id(categoryId)) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_HAS_CHILDREN);
        }

        // 2. 카테고리 삭제
        intangibleAssetCategoryRepository.delete(category);

        return IntangibleAssetCategoryDeleteResponse.builder()
                .categoryId(category.getId())
                .companyId(category.getCompany().getId())
                .build();
    }
}
