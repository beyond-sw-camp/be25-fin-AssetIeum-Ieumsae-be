package com.ieumsae.assetieum.domain.intangibleasset.assignment.service;

import com.ieumsae.assetieum.global.common.util.KstDateTime;

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
import java.time.LocalDateTime;
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
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * 무형자산 배정 이력을 조회
     * 배정 상태별로 필터링하여 목록 반환한다.
     */
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
    public IntangibleAssetAssignmentResponse assignAsset(
            UUID assetId,
            IntangibleAssetAssignmentRequest request,
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
        if (asset.getSeatCount() == 1) {
            // seatCount가 1인 경우, memberId, departmentId, IN_USE 처리
            asset.assignTo(member, department);
        } else {
            // seatCount가 2 이상인 경우, IN_USE 처리만
            asset.markInUse();
        }

        return IntangibleAssetAssignmentResponse.from(savedAssignment);
    }

    /**
     * 무형자산 배정 해지
     * memberId가 있으면 해당 멤버만 해지하고, 없으면 해당 자산의 active 배정 이력을 모두 해지한다.
     */
    @Transactional
    public List<IntangibleAssetAssignmentResponse> cancelAsset(
            UUID assetId,
            UUID memberId,
            UUID companyId
    ) {
        // 1. 입력값 검증
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        if (memberId != null) {
            memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        }

        IntangibleAsset asset = intangibleAssetRepository.findByIdAndCompany_Id(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_FOUND));

        validateCancelableStatus(asset);

        // 2. 배정 이력 해지 처리
        LocalDateTime endedAt = KstDateTime.now();
        if (memberId != null) {
            // 해당 멤버만 해지 처리
            IntangibleAssetAssignment assignment = intangibleAssetAssignmentRepository
                    .findByCompany_IdAndIntangibleAsset_IdAndMember_IdAndAssignmentStatus(
                            companyId,
                            assetId,
                            memberId,
                            AssignmentStatus.ACTIVE
                    )
                    .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ASSIGNMENT_NOT_FOUND));

            assignment.end(endedAt);
            syncAssetStatusAfterSingleMemberEnd(asset, companyId);
            return List.of(IntangibleAssetAssignmentResponse.from(assignment));
        }

        // 해당 자산의 active 배정 이력 모두 해지 처리
        List<IntangibleAssetAssignment> assignments = intangibleAssetAssignmentRepository
                .findAllByCompany_IdAndIntangibleAsset_IdAndAssignmentStatus(
                        companyId,
                        assetId,
                        AssignmentStatus.ACTIVE
                );

        if (assignments.isEmpty()) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_ASSIGNMENT_NOT_FOUND);
        }

        assignments.forEach(assignment -> assignment.end(endedAt));

        // 3. memberId가 null일 때만 해당 자산 CANCELLED 처리
        asset.cancel();

        return assignments.stream()
                .map(IntangibleAssetAssignmentResponse::from)
                .toList();
    }

    @Transactional
    public void reassignAsset(
            UUID assetId,
            UUID currentMemberId,
            UUID newMemberId,
            UUID companyId
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        IntangibleAsset asset = intangibleAssetRepository.findByIdAndCompany_Id(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_FOUND));

        Member newMember = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(newMemberId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (currentMemberId.equals(newMemberId)) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST);
        }

        IntangibleAssetAssignment currentAssignment = intangibleAssetAssignmentRepository
                .findByCompany_IdAndIntangibleAsset_IdAndMember_IdAndAssignmentStatus(
                        companyId,
                        assetId,
                        currentMemberId,
                        AssignmentStatus.ACTIVE
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ASSIGNMENT_NOT_FOUND));

        if (!currentAssignment.getDepartment().getId().equals(newMember.getDepartment().getId())) {
            throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST);
        }

        LocalDateTime originalEndedAt = currentAssignment.getEndedAt();
        LocalDateTime reassignedAt = KstDateTime.now();
        currentAssignment.end(reassignedAt);

        Department department = departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
                        newMember.getDepartment().getId(),
                        companyId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        IntangibleAssetAssignment assignment = IntangibleAssetAssignment.builder()
                .company(company)
                .intangibleAsset(asset)
                .member(newMember)
                .department(department)
                .assignedAt(reassignedAt)
                .endedAt(originalEndedAt)
                .assignmentStatus(AssignmentStatus.ACTIVE)
                .build();

        IntangibleAssetAssignment savedAssignment = intangibleAssetAssignmentRepository.save(assignment);

        if (asset.getSeatCount() == 1) {
            asset.assignTo(newMember, department);
        } else {
            asset.markInUse();
        }

    }

    @Transactional
    public IntangibleAssetAssignmentResponse transferDepartment(
            UUID assetId,
            UUID memberId,
            Department targetDepartment,
            UUID companyId
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        IntangibleAsset asset = intangibleAssetRepository.findByIdAndCompany_Id(assetId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_FOUND));

        Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        IntangibleAssetAssignment currentAssignment = intangibleAssetAssignmentRepository
                .findByCompany_IdAndIntangibleAsset_IdAndMember_IdAndAssignmentStatus(
                        companyId,
                        assetId,
                        memberId,
                        AssignmentStatus.ACTIVE
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.INTANGIBLE_ASSET_ASSIGNMENT_NOT_FOUND));

        if (targetDepartment == null || !targetDepartment.getCompany().getId().equals(companyId)) {
            throw new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        LocalDateTime movedAt = KstDateTime.now();
        LocalDateTime originalEndedAt = currentAssignment.getEndedAt();
        currentAssignment.end(movedAt);

        asset.transferDepartment(targetDepartment);
        if (asset.getSeatCount() == 1) {
            asset.assignTo(member, targetDepartment);
        } else {
            asset.markInUse();
        }

        IntangibleAssetAssignment assignment = IntangibleAssetAssignment.builder()
                .company(company)
                .intangibleAsset(asset)
                .member(member)
                .department(targetDepartment)
                .assignedAt(movedAt)
                .endedAt(originalEndedAt)
                .assignmentStatus(AssignmentStatus.ACTIVE)
                .build();

        IntangibleAssetAssignment savedAssignment = intangibleAssetAssignmentRepository.save(assignment);

        return IntangibleAssetAssignmentResponse.from(savedAssignment);
    }

    private void validateAssignableSeat(IntangibleAsset asset, UUID companyId) {
        if (asset.getSeatCount() <= 1) {
            if (asset.getIntangibleAssetStatus() != IntangibleAssetStatus.AVAILABLE) {
                throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_NOT_ASSIGNABLE);
            }

            return;
        }

        if (!(asset.getIntangibleAssetStatus() == IntangibleAssetStatus.AVAILABLE ||
                asset.getIntangibleAssetStatus() == IntangibleAssetStatus.IN_USE)) {
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

    private void syncAssetStatusAfterSingleMemberEnd(IntangibleAsset asset, UUID companyId) {
        long activeAssignmentCount = intangibleAssetAssignmentRepository
                .countByCompany_IdAndIntangibleAsset_IdAndAssignmentStatus(
                        companyId,
                        asset.getId(),
                        AssignmentStatus.ACTIVE
                );

        if (activeAssignmentCount > 0) {
            asset.markInUse();
            return;
        }

        asset.makeAvailable();
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

    private void validateCancelableStatus(IntangibleAsset asset) {
        if (asset.getIntangibleAssetStatus() == IntangibleAssetStatus.IN_USE) {
            return;
        }

        throw new BusinessException(ErrorCode.INTANGIBLE_ASSET_INVALID_REQUEST);
    }
}
