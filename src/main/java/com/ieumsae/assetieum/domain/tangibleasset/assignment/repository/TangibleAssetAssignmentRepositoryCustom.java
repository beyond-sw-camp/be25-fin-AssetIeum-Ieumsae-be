package com.ieumsae.assetieum.domain.tangibleasset.assignment.repository;

import com.ieumsae.assetieum.domain.tangibleasset.assignment.dto.TangibleAssetAssignmentSearchResponse;
import com.ieumsae.assetieum.domain.tangibleasset.assignment.type.AssignmentStatus;
import java.util.List;
import java.util.UUID;

public interface TangibleAssetAssignmentRepositoryCustom {

    List<TangibleAssetAssignmentSearchResponse> search(
            UUID companyId,
            UUID assetId,
            AssignmentStatus assignmentStatus
    );
}
