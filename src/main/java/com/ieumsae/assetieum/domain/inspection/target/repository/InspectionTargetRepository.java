package com.ieumsae.assetieum.domain.inspection.target.repository;

import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InspectionTargetRepository extends JpaRepository<InspectionTarget, UUID> {
}
