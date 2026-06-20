package com.ieumsae.assetieum.domain.inspection.followup.repository;

import com.ieumsae.assetieum.domain.inspection.followup.entity.InspectionFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InspectionFollowUpRepository extends JpaRepository<InspectionFollowUp, UUID> {

    List<InspectionFollowUp> findAllByInspectionResult_IdInAndCompany_Id(List<UUID> inspectionResultIds, UUID companyId);
}
