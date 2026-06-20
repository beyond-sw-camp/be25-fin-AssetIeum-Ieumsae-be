package com.ieumsae.assetieum.domain.inspection.inspection.repository;

import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface InspectionRepository extends JpaRepository<Inspection, UUID> {

    @Query(
            value = """
                    SELECT i
                    FROM Inspection i
                    JOIN FETCH i.inspector
                    LEFT JOIN FETCH i.targetDepartment
                    WHERE i.company.id = :companyId
                      AND i.inspectionType = :inspectionType
                      AND (:status IS NULL OR i.inspectionStatus = :status)
                      AND (:inspectorId IS NULL OR i.inspector.id = :inspectorId)
                    """,
            countQuery = """
                    SELECT COUNT(i)
                    FROM Inspection i
                    WHERE i.company.id = :companyId
                      AND i.inspectionType = :inspectionType
                      AND (:status IS NULL OR i.inspectionStatus = :status)
                      AND (:inspectorId IS NULL OR i.inspector.id = :inspectorId)
                    """
    )
    Page<Inspection> search(
            @Param("companyId") UUID companyId,
            @Param("inspectionType") InspectionType inspectionType,
            @Param("status") InspectionStatus status,
            @Param("inspectorId") UUID inspectorId,
            Pageable pageable
    );
}
