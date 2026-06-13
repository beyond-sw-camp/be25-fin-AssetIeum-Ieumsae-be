package com.ieumsae.assetieum.domain.intangibleasset.assignment.repository;

import com.ieumsae.assetieum.domain.intangibleasset.assignment.dto.IntangibleAssetAssignmentResponse;
import com.ieumsae.assetieum.domain.intangibleasset.assignment.type.AssignmentStatus;

import java.util.List;
import java.util.UUID;

public interface IntangibleAssetAssignmentRepositoryCustom {

    List<IntangibleAssetAssignmentResponse> search(
            UUID companyId,
            UUID assetId,
            AssignmentStatus assignmentStatus
    );
}
