package com.ieumsae.assetieum.domain.intangibleasset.service;

import com.ieumsae.assetieum.domain.company.Company;
import com.ieumsae.assetieum.domain.company.CompanyRepository;
import com.ieumsae.assetieum.domain.intangibleasset.dto.IntangibleAssetCategoryCreateRequest;
import com.ieumsae.assetieum.domain.intangibleasset.dto.IntangibleAssetCategoryResponse;
import com.ieumsae.assetieum.domain.intangibleasset.entity.IntangibleAssetCategory;
import com.ieumsae.assetieum.domain.intangibleasset.repository.IntangibleAssetCategoryRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
