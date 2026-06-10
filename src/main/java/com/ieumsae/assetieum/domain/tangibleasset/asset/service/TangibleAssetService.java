package com.ieumsae.assetieum.domain.tangibleasset.asset.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetCreateRequest;
import com.ieumsae.assetieum.domain.tangibleasset.asset.dto.TangibleAssetResponse;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.item.entity.TangibleAssetItem;
import com.ieumsae.assetieum.domain.tangibleasset.item.repository.TangibleAssetItemRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
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
    private final CompanyRepository companyRepository;
    private final CodeGenerator codeGenerator;

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

        TangibleAssetItem item = tangibleAssetItemRepository.findByIdAndDeletedAtIsNull(request.getTangibleItemId())
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
}
