package com.ieumsae.assetieum.domain.inspection.target.repository;

import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionTargetRepository extends JpaRepository<InspectionTarget, UUID> {
}
