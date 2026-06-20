package com.ieumsae.assetieum.domain.inspection.inspection.repository;

import com.ieumsae.assetieum.domain.inspection.inspection.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InspectionRepository extends JpaRepository<Inspection, UUID>, InspectionRepositoryCustom {
}
