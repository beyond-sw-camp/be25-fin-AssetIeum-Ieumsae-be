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

    @Transactional
    public IntangibleAssetCategoryResponse createCategory(
            IntangibleAssetCategoryCreateRequest request
    ) {
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

    public List<IntangibleAssetCategoryTreeResponse> getIntangibleCategories(
            UUID companyId) {
        if(!companyRepository.existsById(companyId)) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }

        List<IntangibleAssetCategory> categories =
                intangibleAssetCategoryRepository.findAllByCompany_IdOrderByCreatedAtAsc(
                        companyId
                );

        Map<UUID, IntangibleAssetCategoryTreeResponse> categoryMap =
                new LinkedHashMap<>();

        List<IntangibleAssetCategoryTreeResponse> roots =
                new ArrayList<>();

        for(IntangibleAssetCategory category : categories) {
            categoryMap.put(
                    category.getId(),
                    IntangibleAssetCategoryTreeResponse.from(category)
            );
        }

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

    @Transactional
    public IntangibleAssetCategoryDeleteResponse deleteCategory(UUID categoryId) {
        IntangibleAssetCategory category =
                intangibleAssetCategoryRepository.findById(categoryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_NOT_FOUND));

        if(intangibleAssetCategoryRepository.existsByParent_Id(categoryId)) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_CATEGORY_HAS_CHILDREN);
        }

        intangibleAssetCategoryRepository.delete(category);

        return IntangibleAssetCategoryDeleteResponse.builder()
                .categoryId(category.getId())
                .companyId(category.getCompany().getId())
                .build();
    }
}
