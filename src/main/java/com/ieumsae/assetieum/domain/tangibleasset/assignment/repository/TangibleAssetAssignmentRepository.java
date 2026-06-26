package com.ieumsae.assetieum.domain.tangibleasset.assignment.repository;

import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.TangibleAssetStatus;
import com.ieumsae.assetieum.domain.tangibleasset.asset.type.UsageType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TangibleAssetAssignmentRepository extends JpaRepository<TangibleAssetAssignment, UUID>, TangibleAssetAssignmentRepositoryCustom {
    Optional<TangibleAssetAssignment> findByCompany_IdAndTangibleAsset_IdAndAssignmentStatus(
            UUID companyId,
            UUID assetId,
            AssignmentStatus assignmentStatus
    );

    List<TangibleAssetAssignment> findAllByCompany_IdAndMember_IdAndAssignmentStatus(
            UUID companyId,
            UUID memberId,
            AssignmentStatus assignmentStatus
    );

    Optional<TangibleAssetAssignment> findByIdAndCompany_IdAndMember_IdAndAssignmentStatus(
            UUID assignmentId,
            UUID companyId,
            UUID memberId,
            AssignmentStatus assignmentStatus
    );

    @EntityGraph(attributePaths = {"member", "tangibleAsset", "tangibleAsset.tangibleAssetItem"})
    List<TangibleAssetAssignment> findAllByAssignmentStatusAndAssignmentTypeAndTangibleAsset_TangibleAssetStatusAndTangibleAsset_ReturnDueDateGreaterThanEqualAndTangibleAsset_ReturnDueDateLessThan(
            AssignmentStatus assignmentStatus,
            UsageType assignmentType,
            TangibleAssetStatus tangibleAssetStatus,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    @EntityGraph(attributePaths = {"member", "tangibleAsset", "tangibleAsset.tangibleAssetItem"})
    List<TangibleAssetAssignment> findAllByAssignmentStatusAndAssignmentTypeAndTangibleAsset_TangibleAssetStatusAndTangibleAsset_ReturnDueDateLessThan(
            AssignmentStatus assignmentStatus,
            UsageType assignmentType,
            TangibleAssetStatus tangibleAssetStatus,
            LocalDateTime endExclusive
    );
}
