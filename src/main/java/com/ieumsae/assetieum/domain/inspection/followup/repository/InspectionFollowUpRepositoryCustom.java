package com.ieumsae.assetieum.domain.inspection.followup.repository;

import com.ieumsae.assetieum.domain.inspection.followup.entity.InspectionFollowUp;
import com.ieumsae.assetieum.domain.inspection.followup.type.InspectionFollowUpStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InspectionFollowUpRepositoryCustom {

    Page<InspectionFollowUp> searchFollowUps(
            UUID companyId,
            UUID inspectorId,
            InspectionFollowUpStatus status,
            String keyword,
            Pageable pageable
    );
}
