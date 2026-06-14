package com.ieumsae.assetieum.domain.tangibleasset.assignment.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.entity.TangibleAsset;
import com.ieumsae.assetieum.domain.tangibleasset.asset.repository.TangibleAssetRepository;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.dto.TangibleAssetAssignmentRequest;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.dto.TangibleAssetAssignmentResponse;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.repository.TangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TangibleAssetAssignmentService {

    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final TangibleAssetRepository tangibleAssetRepository;
    private final TangibleAssetAssignmentRepository tangibleAssetAssignmentRepository;

    public List<TangibleAssetAssignmentResponse> getAssignments(
            UUID assetId,
            AssignmentStatus assignmentStatus,
            UUID companyId
    ) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        tangibleAssetRepository.findByIdAndCompany_Id(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));

        return tangibleAssetAssignmentRepository.search(companyId, assetId, assignmentStatus);
    }

    /**
     * 유형자산을 사용자에게 배정.
     * 자산의 상태를 변경하고, 배정 이력을 생성한다.
     */
    @Transactional
    public TangibleAssetAssignmentResponse assignAsset(
            UUID assetId,
            TangibleAssetAssignmentRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getMemberId(), companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Department department = departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
                        member.getDepartment().getId(),
                        companyId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        TangibleAsset asset = tangibleAssetRepository.findByIdAndCompany_Id(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));

        validateAssignmentRequest(request);
        validateAssignableSeat(asset);

        // 2. 배정 이력 생성
        TangibleAssetAssignment assignment = TangibleAssetAssignment.builder()
                .company(company)
                .tangibleAsset(asset)
                .member(member)
                .department(department)
                .assignmentType(request.getUsageType())
                .endedAt(request.getEndedAt())
                .assignmentStatus(AssignmentStatus.ACTIVE)
                .build();

        TangibleAssetAssignment savedAssignment = tangibleAssetAssignmentRepository.save(assignment);

        // 3. 해당 자산 사용중 처리
        asset.markInUse(
                member,
                department,
                request.getUsageType(),
                request.getAssetUsageType(),
                savedAssignment.getAssignedAt(),
                request.getEndedAt()
        );

        return TangibleAssetAssignmentResponse.from(savedAssignment);
    }

    private void validateAssignableSeat(TangibleAsset asset) {
        if (asset.getTangibleAssetStatus() != TangibleAssetStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_ASSIGNABLE);
        }
    }

    private void validateAssignmentRequest(TangibleAssetAssignmentRequest request) {
        if (request.getUsageType() == UsageType.TEMPORARY && request.getEndedAt() == null) {
            throw new BusinessException(
                    ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST,
                    "임시 배정은 종료일이 필수입니다."
            );
        }
    }

    /**
     * 유형자산 배정 해지
     * 해당 자산을 RETURN_REQUESTED 상태로 변경한다.
     */
    @Transactional
    public TangibleAssetAssignmentResponse cancelAsset(
            UUID assetId,
            UUID companyId
    ) {
        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        TangibleAsset asset = tangibleAssetRepository.findByIdAndCompany_Id(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_NOT_FOUND));

        validateCancelableStatus(asset);

        // 2. 배정 이력 해지 처리
        LocalDateTime endedAt = LocalDateTime.now();
        TangibleAssetAssignment assignment = tangibleAssetAssignmentRepository.findByCompany_IdAndTangibleAsset_IdAndAssignmentStatus(
                companyId,
                assetId,
                AssignmentStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.TANGIBLE_ASSET_ASSIGNMENT_NOT_FOUND));

        assignment.end(endedAt);

        // 3. 해당 자산 RETURN_REQUESTED 처리
        asset.returnRequest();

        return TangibleAssetAssignmentResponse.from(assignment);

    }

    private void validateCancelableStatus(TangibleAsset asset) {
        if (asset.getTangibleAssetStatus() == TangibleAssetStatus.IN_USE) {
            return;
        }

        throw new BusinessException(ErrorCode.TANGIBLE_ASSET_INVALID_REQUEST);
    }
}
