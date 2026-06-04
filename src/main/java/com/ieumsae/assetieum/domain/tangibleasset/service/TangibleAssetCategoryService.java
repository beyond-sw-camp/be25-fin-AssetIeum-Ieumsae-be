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

    @Transactional
    public TangibleAssetCategoryResponse createCategory(
            TangibleAssetCategoryCreateRequest request
    ) {
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

    public List<TangibleAssetCategoryTreeResponse> getTangibleCategories(
            UUID companyId
    ) {
        if (!companyRepository.existsById(companyId)) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }

        List<TangibleAssetCategory> categories =
                tangibleAssetCategoryRepository.findAllByCompany_IdOrderByCreatedAtAsc(
                        companyId
                );

        Map<UUID, TangibleAssetCategoryTreeResponse> categoryMap =
                new LinkedHashMap<>();

        List<TangibleAssetCategoryTreeResponse> roots =
                new ArrayList<>();

        for (TangibleAssetCategory category : categories) {
            categoryMap.put(
                    category.getId(),
                    TangibleAssetCategoryTreeResponse.from(category)
            );
        }

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

    @Transactional
    public TangibleAssetCategoryDeleteResponse deleteCategory(UUID categoryId) {
        TangibleAssetCategory category =
                tangibleAssetCategoryRepository.findById(categoryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));

        if(tangibleAssetCategoryRepository.existsByParent_Id(categoryId)) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_HAS_CHILDREN);
        }

        tangibleAssetCategoryRepository.delete(category);

        return TangibleAssetCategoryDeleteResponse.builder()
                .categoryId(category.getId())
                .companyId(category.getCompany().getId())
                .build();
    }
}
