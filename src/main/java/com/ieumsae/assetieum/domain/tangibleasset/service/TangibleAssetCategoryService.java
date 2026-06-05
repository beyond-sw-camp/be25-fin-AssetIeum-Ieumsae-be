package com.ieumsae.assetieum.domain.tangibleasset.service;

import com.ieumsae.assetieum.domain.company.Company;
import com.ieumsae.assetieum.domain.company.CompanyRepository;
import com.ieumsae.assetieum.domain.tangibleasset.dto.TangibleAssetCategoryCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.dto.TangibleAssetCategoryDeleteResponse;
import com.ieumsae.assetieum.domain.tangibleasset.dto.TangibleAssetCategoryResponse;
import com.ieumsae.assetieum.domain.tangibleasset.dto.TangibleAssetCategoryTreeResponse;
import com.ieumsae.assetieum.domain.tangibleasset.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.repository.TangibleAssetCategoryRepository;
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
public class TangibleAssetCategoryService {

    private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
    private final CompanyRepository companyRepository;

    /**
     * 유형자산 카테고리 등록.
     * 동일 회사 내 카테고리명 중복 여부를 검증하고,
     * parentId가 존재할 경우 부모 카테고리 유효성 및
     * 동일 회사 소속 여부를 함께 검증한다.
     */
    @Transactional
    public TangibleAssetCategoryResponse createCategory(
            TangibleAssetCategoryCreateRequest request
    ) {
        // 1. 입력값 검증
        if (tangibleAssetCategoryRepository.existsByCompany_IdAndName(
                request.getCompanyId(),
                request.getName()
        )) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_ALREADY_EXISTS);
        }

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        TangibleAssetCategory parent = null;
        if(request.getParentId() != null){
            parent = tangibleAssetCategoryRepository.findById(
                    request.getParentId()
            ).orElseThrow(() ->
                    new BusinessException(
                            ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND
                    ));

            if(!parent.getCompany().getId().equals(request.getCompanyId())) {
                throw new BusinessException(ErrorCode.TANGIBLE_ASSET_INVALID_PARENT);
            }
        }

        // 2. 카테고리 생성 및 저장
        TangibleAssetCategory category = TangibleAssetCategory.builder()
                .company(company)
                .parent(parent)
                .name(request.getName())
                .build();

        TangibleAssetCategory savedCategory = tangibleAssetCategoryRepository.save(category);

        return TangibleAssetCategoryResponse.from(
                savedCategory
        );

    }

    /**
     * 회사 기준 유형자산 카테고리 목록 조회.
     * 전체 카테고리를 조회한 뒤 parent 관계를 기준으로
     * children을 연결하여 트리 구조로 반환한다.
     */
    public List<TangibleAssetCategoryTreeResponse> getTangibleCategories(
            UUID companyId
    ) {
        // 1. 입력값 검증
        if (!companyRepository.existsById(companyId)) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }

        // 2. 회사에 속한 전체 카테고리 조회
        List<TangibleAssetCategory> categories =
                tangibleAssetCategoryRepository.findAllByCompany_IdOrderByCreatedAtAsc(
                        companyId
                );

        Map<UUID, TangibleAssetCategoryTreeResponse> categoryMap =
                new LinkedHashMap<>();

        // roots: parent가 없는 최상위 카테고리 목록
        List<TangibleAssetCategoryTreeResponse> roots =
                new ArrayList<>();

        for (TangibleAssetCategory category : categories) {
            categoryMap.put(
                    category.getId(),
                    TangibleAssetCategoryTreeResponse.from(category)
            );
        }

        // 3. 각 카테고리의 parent 정보를 기준으로 트리 구조 구성
        for (TangibleAssetCategory category : categories) {
            TangibleAssetCategoryTreeResponse response =
                    categoryMap.get(category.getId());

            if (category.getParent() == null) {
                roots.add(response);
                continue;
            }

            TangibleAssetCategoryTreeResponse parent =
                    categoryMap.get(category.getParent().getId());

            if (parent != null) {
                parent.addChild(response);
            }
        }

        return roots;
    }

    /**
     * 유형자산 카테고리 삭제.
     * 하위 카테고리가 존재하는 경우 삭제를 제한한다.
     */
    @Transactional
    public TangibleAssetCategoryDeleteResponse deleteCategory(UUID categoryId) {
        // 1. 입력값 검증
        TangibleAssetCategory category =
                tangibleAssetCategoryRepository.findById(categoryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));

        if(tangibleAssetCategoryRepository.existsByParent_Id(categoryId)) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_HAS_CHILDREN);
        }

        // 2. 카테고리 삭제
        tangibleAssetCategoryRepository.delete(category);

        return TangibleAssetCategoryDeleteResponse.builder()
                .categoryId(category.getId())
                .companyId(category.getCompany().getId())
                .build();
    }
}
