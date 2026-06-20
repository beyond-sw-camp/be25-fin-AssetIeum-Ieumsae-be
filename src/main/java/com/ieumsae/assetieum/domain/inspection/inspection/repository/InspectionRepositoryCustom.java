package com.ieumsae.assetieum.domain.inspection.inspection.repository;

import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.result.entity.InspectionResult;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InspectionRepositoryCustom {

    Page<Inspection> search(
            UUID companyId,
            InspectionType inspectionType,
            InspectionStatus status,
            UUID inspectorId,
            Pageable pageable
    );

    Optional<Inspection> findDetailByIdAndCompanyIdAndInspectionType(
            UUID inspectionId,
            UUID companyId,
            InspectionType inspectionType
    );

    List<InspectionTarget> findTargetsWithAssets(UUID inspectionId, UUID companyId);

    List<InspectionResult> findResults(UUID inspectionId, UUID companyId);
}
