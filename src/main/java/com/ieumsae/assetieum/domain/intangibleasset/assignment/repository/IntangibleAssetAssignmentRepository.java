package com.ieumsae.assetieum.domain.intangibleasset.assignment.repository;

import com.ieumsae.assetieum.domain.intangibleasset.assignment.entity.IntangibleAssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IntangibleAssetAssignmentRepository extends JpaRepository<IntangibleAssetAssignment, UUID>, IntangibleAssetAssignmentRepositoryCustom {
}
