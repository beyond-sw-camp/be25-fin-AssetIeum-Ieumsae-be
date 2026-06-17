package com.ieumsae.assetieum.domain.tangibleasset.assignment.repository;

import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
