package com.ieumsae.assetieum.domain.inspection.target.repository;

import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InspectionTargetRepositoryCustom {

    Page<InspectionTarget> searchMyTargets(
            UUID companyId,
            UUID memberId,
            InspectionType inspectionType,
            InspectionStatus status,
            Boolean isResponded,
            Pageable pageable
    );

    Page<InspectionTarget> searchInspectorTargets(
            UUID companyId,
            UUID inspectorId,
            InspectionType inspectionType,
            InspectionStatus status,
            Boolean isResponded,
            Pageable pageable
    );
}
