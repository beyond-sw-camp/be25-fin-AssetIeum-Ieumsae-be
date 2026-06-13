package com.ieumsae.assetieum.domain.intangibleasset.assignment.service;

import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.dto.IntangibleAssetAssignmentSearchResponse;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntangibleAssetAssignmentService {

    private final CompanyRepository companyRepository;
    private final IntangibleAssetRepository intangibleAssetRepository;
    private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;

    public List<IntangibleAssetAssignmentSearchResponse> getAssignments(
            UUID assetId,
            AssignmentStatus assignmentStatus,
            UUID companyId
    ) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        intangibleAssetRepository.findByIdAndCompany_Id(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_FOUND));

        return intangibleAssetAssignmentRepository.search(companyId, assetId, assignmentStatus);
    }
}
