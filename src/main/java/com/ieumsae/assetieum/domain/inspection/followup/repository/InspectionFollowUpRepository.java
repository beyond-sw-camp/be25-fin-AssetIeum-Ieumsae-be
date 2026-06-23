package com.ieumsae.assetieum.domain.inspection.followup.repository;

import com.ieumsae.assetieum.domain.inspection.followup.entity.InspectionFollowUp;
import com.ieumsae.assetieum.domain.inspection.followup.type.InspectionFollowUpStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InspectionFollowUpRepository extends JpaRepository<InspectionFollowUp, UUID>, InspectionFollowUpRepositoryCustom {

    List<InspectionFollowUp> findAllByInspectionResult_IdInAndCompany_Id(List<UUID> inspectionResultIds, UUID companyId);

    Optional<InspectionFollowUp> findByIdAndCompany_Id(UUID followUpId, UUID companyId);

    Optional<InspectionFollowUp> findByInspectionResult_IdAndCompany_Id(UUID inspectionResultId, UUID companyId);

    boolean existsByInspectionResult_Inspection_IdAndCompany_IdAndInspectionFollowUpStatusNot(
            UUID inspectionId,
            UUID companyId,
            InspectionFollowUpStatus status
    );
}
