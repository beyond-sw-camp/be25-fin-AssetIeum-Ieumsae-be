package com.ieumsae.assetieum.domain.inspection.target.repository;

import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InspectionTargetRepository
        extends JpaRepository<InspectionTarget, UUID>, InspectionTargetRepositoryCustom {

    Optional<InspectionTarget> findByIdAndCompany_Id(UUID targetId, UUID companyId);

}
