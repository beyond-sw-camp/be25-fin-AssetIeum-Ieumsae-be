package com.ieumsae.assetieum.domain.tangibleasset.service;

import com.ieumsae.assetieum.domain.company.Company;
import com.ieumsae.assetieum.domain.company.CompanyRepository;
import com.ieumsae.assetieum.domain.tangibleasset.dto.TangibleAssetCategoryCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.dto.TangibleAssetCategoryResponse;
import com.ieumsae.assetieum.domain.tangibleasset.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                throw new BusinessException(ErrorCode.INVALID_PARENT_CATEGORY);
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
}
