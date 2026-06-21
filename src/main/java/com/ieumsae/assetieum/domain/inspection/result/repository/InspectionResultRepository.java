package com.ieumsae.assetieum.domain.inspection.result.repository;

import com.ieumsae.assetieum.domain.inspection.result.entity.InspectionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InspectionResultRepository extends JpaRepository<InspectionResult, UUID> {

    List<InspectionResult> findAllByInspection_IdAndCompany_Id(UUID inspectionId, UUID companyId);

    boolean existsByInspectionTarget_IdAndCompany_Id(UUID inspectionTargetId, UUID companyId);
}
