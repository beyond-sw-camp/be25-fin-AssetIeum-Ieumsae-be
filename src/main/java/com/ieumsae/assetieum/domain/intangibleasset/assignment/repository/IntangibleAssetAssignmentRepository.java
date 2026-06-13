package com.ieumsae.assetieum.domain.intangibleasset.assignment.repository;

import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IntangibleAssetAssignmentRepository extends JpaRepository<IntangibleAssetAssignment, UUID>, IntangibleAssetAssignmentRepositoryCustom {

    long countByCompany_IdAndIntangibleAsset_IdAndAssignmentStatus(
            UUID companyId,
            UUID assetId,
            AssignmentStatus assignmentStatus
    );

    boolean existsByCompany_IdAndIntangibleAsset_IdAndMember_IdAndAssignmentStatus(
            UUID companyId,
            UUID assetId,
            UUID memberId,
            AssignmentStatus assignmentStatus
    );
}
