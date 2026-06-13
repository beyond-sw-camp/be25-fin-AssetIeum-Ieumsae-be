package com.ieumsae.assetieum.domain.intangibleasset.assignment.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.entity.IntangibleAsset;
import com.ieumsae.assetieum.domain.intangibleasset.asset.repository.IntangibleAssetRepository;
import com.ieumsae.assetieum.domain.intangibleasset.asset.type.IntangibleAssetStatus;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.dto.IntangibleAssetAssignmentRequest;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.dto.IntangibleAssetAssignmentResponse;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.repository.IntangibleAssetAssignmentRepository;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntangibleAssetAssignmentService {

    private final CompanyRepository companyRepository;
    private final IntangibleAssetRepository intangibleAssetRepository;
    private final IntangibleAssetAssignmentRepository intangibleAssetAssignmentRepository;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;

    public List<IntangibleAssetAssignmentResponse> getAssignments(
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

    /**
     * 무형자산을 사용자에게 배정
     * 자산의 상태를 변경하고, 배정 이력을 생성한다.
     */
    @Transactional
    public IntangibleAssetAssignmentResponse assign(
            UUID assetId,
            IntangibleAssetAssignmentRequest request,
            UUID companyId
    ) {
        // 1. 입력값 검증

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getMemberId(), companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Department department = departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(member.getDepartment().getId(), companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));


        IntangibleAsset asset = intangibleAssetRepository.findByIdAndCompany_Id(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_FOUND));

        validateAssignableSeat(asset, companyId);
        validateNotAlreadyAssigned(asset.getId(), member.getId(), companyId);

        // 2. 배정 이력 생성
        IntangibleAssetAssignment assignment = IntangibleAssetAssignment.builder()
                .company(company)
                .intangibleAsset(asset)
                .member(member)
                .department(department)
                .endedAt(request.getEndedAt())
                .assignmentStatus(AssignmentStatus.ACTIVE)
                .build();

        IntangibleAssetAssignment savedAssignment = intangibleAssetAssignmentRepository.save(assignment);

        // 3. 해당 자산 사용중 처리
        if(asset.getSeatCount() == 1){
            // seatCount가 1인 경우, memberId, departmentId, IN_USE 처리
            asset.assignTo(member, department);
        } else {
            // seatCount가 이상인 경우, IN_USE 처리만
            asset.markInUse();
        }

        return IntangibleAssetAssignmentResponse.from(savedAssignment);
    }

    private void validateAssignableSeat(IntangibleAsset asset, UUID companyId) {
        if (asset.getSeatCount() <= 1) {
            if(asset.getIntangibleAssetStatus() != IntangibleAssetStatus.AVAILABLE){
                throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_ASSIGNABLE);
            }

            return;
        }

        if(!(asset.getIntangibleAssetStatus() == IntangibleAssetStatus.AVAILABLE ||
                asset.getIntangibleAssetStatus() == IntangibleAssetStatus.IN_USE)
        ){
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_ASSIGNABLE);
        }

        long activeAssignmentCount = intangibleAssetAssignmentRepository
                .countByCompany_IdAndIntangibleAsset_IdAndAssignmentStatus(
                        companyId,
                        asset.getId(),
                        AssignmentStatus.ACTIVE
                );

        if (activeAssignmentCount >= asset.getSeatCount()) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_ASSIGNABLE);
        }
    }

    private void validateNotAlreadyAssigned(UUID assetId, UUID memberId, UUID companyId) {
        boolean alreadyAssigned = intangibleAssetAssignmentRepository
                .existsByCompany_IdAndIntangibleAsset_IdAndMember_IdAndAssignmentStatus(
                        companyId,
                        assetId,
                        memberId,
                        AssignmentStatus.ACTIVE
                );

        if (alreadyAssigned) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_ASSIGNABLE);
        }
    }
}
