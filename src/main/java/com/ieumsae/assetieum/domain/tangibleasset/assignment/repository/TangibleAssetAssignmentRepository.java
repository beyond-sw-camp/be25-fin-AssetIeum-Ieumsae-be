package com.ieumsae.assetieum.domain.tangibleasset.assignment.repository;

import com.ieumsae.assetieum.domain.tangibleasset.assignment.entity.TangibleAssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TangibleAssetAssignmentRepository extends JpaRepository<TangibleAssetAssignment, UUID>, TangibleAssetAssignmentRepositoryCustom {
}
