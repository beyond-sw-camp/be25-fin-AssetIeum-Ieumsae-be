package com.ieumsae.assetieum.domain.tangibleasset.asset.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetSearchResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.category.entity.TangibleAssetCategory;
import com.ieumsae.assetieum.domain.tangibleasset.category.repository.TangibleAssetCategoryRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
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

    /**
     * 유형자산 등록
     * 동일 회사 내 시리얼 번호 중복 여부를 검증한다.
     */
    @Transactional
    public TangibleAssetResponse createAsset(
            TangibleAssetCreateRequest request
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getTangibleItemId(), request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));

        if(tangibleAssetRepository.existsByCompany_IdAndSerialNumberAndTangibleAssetItem_Id(
                request.getCompanyId(),
                request.getSerialNumber(),
                request.getTangibleItemId()
        )) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ITEM_DUPLICATED_SERIAL_NUMBER);
        }

        // 2. 자산 생성 및 저장
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

    /**
     * 회사 기준 유형자산 목록 조회
     * 카테고리, 품목 ID, 상태, 키워드, 현재 사용자, 부서를 기준으로 필터링하여
     * 해당하는 자산만 조회하여 반환한다.
     */
    public PaginationResponse<TangibleAssetSearchResponse> getAssets(
            TangibleAssetSearchRequest request
    ) {
        // 1. 입력값 검증
        companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));


        if(request.getCategoryId() != null) {
            tangibleAssetCategoryRepository.findByIdAndCompany_Id(request.getCategoryId(), request.getCompanyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_CATEGORY_NOT_FOUND));
        }


        if(request.getTangibleItemId() != null) {
            tangibleAssetItemRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getTangibleItemId(), request.getCompanyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ITEM_NOT_FOUND));
        }

        if(request.getCurrentUserId() != null) {
            memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getCurrentUserId(), request.getCompanyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        }

        if(request.getDepartmentId() != null) {
            departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getDepartmentId(), request.getCompanyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        }

        // 2. 페이징 처리 및 필터링 후 자산 목록 반환
        Page<TangibleAssetSearchResponse> assetPage =
                tangibleAssetRepository.search(
                        request.getCompanyId(),
                        request.getCategoryId(),
                        request.getTangibleItemId(),
                        request.getStatus(),
                        request.getKeyword(),
                        request.getCurrentUserId(),
                        request.getDepartmentId(),
                        request.toPageable()
                );

        return PaginationResponse.from(assetPage);

    }
}
