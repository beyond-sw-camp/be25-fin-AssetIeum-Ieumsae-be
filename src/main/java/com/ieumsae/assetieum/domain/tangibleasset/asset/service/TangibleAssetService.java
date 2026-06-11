package com.ieumsae.assetieum.domain.tangibleasset.asset.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetDetailResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetUpdateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TangibleAssetService {

    private static final String TANGIBLE_ASSET_CODE_PREFIX = "TA";
    private static final String REDIS_KEY_PREFIX = "tangible-asset:code:";

    private final TangibleAssetRepository tangibleAssetRepository;
    private final TangibleAssetItemRepository tangibleAssetItemRepository;
    private final TangibleAssetCategoryRepository tangibleAssetCategoryRepository;
    private final CompanyRepository companyRepository;
    private final CodeGenerator codeGenerator;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public TangibleAssetResponse createAsset(
            TangibleAssetCreateRequest request
    ) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getTangibleItemId(), request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

        if(tangibleAssetRepository.existsByCompany_IdAndSerialNumberAndTangibleAssetItem_Id(
                request.getCompanyId(),
                request.getSerialNumber(),
                request.getTangibleItemId()
        )) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_DUPLICATED_SERIAL_NUMBER);
        }

        TangibleAsset asset = TangibleAsset.builder()
                .company(company)
                .tangibleAssetItem(item)
                .usageType(request.getUsageType())
                .assetUsageType(request.getAssetUsageType())
                .assetCode(codeGenerator.generate(TANGIBLE_ASSET_CODE_PREFIX, REDIS_KEY_PREFIX))
                .serialNumber(request.getSerialNumber())
                .location(request.getLocation())
                .purchaseDate(request.getPurchaseDate())
                .purchasePrice(request.getPurchasePrice())
                .purchaseVendor(request.getPurchaseVendor())
                .warrantyExpiredAt(request.getWarrantyExpiredAt())
                .build();

        TangibleAsset savedAsset = tangibleAssetRepository.save(asset);

        return TangibleAssetResponse.from(savedAsset);
    }

    public PaginationResponse<TangibleAssetSearchResponse> getAssets(
            TangibleAssetSearchRequest request
    ) {
        companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        if(request.getCategoryId() != null) {
            tangibleAssetCategoryRepository.findByIdAndCompany_Id(request.getCategoryId(), request.getCompanyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));
        }

        if(request.getCurrentUserId() != null) {
            memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getCurrentUserId(), request.getCompanyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        }

        if(request.getDepartmentId() != null) {
            departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getDepartmentId(), request.getCompanyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        }

        Page<TangibleAssetSearchResponse> assetPage =
                tangibleAssetRepository.search(
                        request.getCompanyId(),
                        request.getCategoryId(),
                        request.getStatus(),
                        request.getKeyword(),
                        request.getCurrentUserId(),
                        request.getDepartmentId(),
                        request.toPageable()
                );

        return PaginationResponse.from(assetPage);
    }

    public TangibleAssetDetailResponse getAssetDetail(UUID assetId, UUID companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        return tangibleAssetRepository.findDetailByIdAndCompanyId(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));
    }

    @Transactional
    public TangibleAssetResponse updateAsset(UUID assetId, TangibleAssetUpdateRequest request, UUID companyId) {

        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        TangibleAsset asset = tangibleAssetRepository.findByIdAndCompany_Id(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getDepartmentId(),companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
        }

        Member member = null;
        if (request.getMemberId() != null) {
            member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getMemberId(),companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        }

        validateReturnDueDate(request, asset);

        // 2. 유형자산 수정
        asset.update(request, department, member);

        return TangibleAssetResponse.from(asset);
    }

    private void validateReturnDueDate(TangibleAssetUpdateRequest request, TangibleAsset asset) {
        if (request.getReturnDueDate() == null) {
            return;
        }

        UsageType usageType = request.getUsageType() != null
                ? request.getUsageType()
                : asset.getUsageType();

        if (usageType == UsageType.PERMANENT) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST);
        }

        LocalDateTime usedStartedAt = request.getUsedStartedAt() != null
                ? request.getUsedStartedAt()
                : asset.getUsedStartedAt();

        if (usedStartedAt == null) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST);
        }

        if (usedStartedAt.isAfter(request.getReturnDueDate())) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST);
        }
    }
}
