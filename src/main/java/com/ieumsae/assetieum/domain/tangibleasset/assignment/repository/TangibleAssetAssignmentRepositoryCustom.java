package com.ieumsae.assetieum.domain.tangibleasset.assignment.repository;

import com.ieumsae.assetieum.domain.tangibleasset.assignment.dto.TangibleAssetAssignmentResponse;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;

import java.util.List;
import java.util.UUID;

public interface TangibleAssetAssignmentRepositoryCustom {

    List<TangibleAssetAssignmentResponse> search(
            UUID companyId,
            UUID assetId,
            AssignmentStatus assignmentStatus
    );
}
