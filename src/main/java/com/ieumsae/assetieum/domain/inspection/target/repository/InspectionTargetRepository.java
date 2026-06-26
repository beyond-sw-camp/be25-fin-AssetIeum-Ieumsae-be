package com.ieumsae.assetieum.domain.inspection.target.repository;

import com.ieumsae.assetieum.domain.inspection.target.entity.InspectionTarget;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface InspectionTargetRepository
        extends JpaRepository<InspectionTarget, UUID>, InspectionTargetRepositoryCustom {

    Optional<InspectionTarget> findByIdAndCompany_Id(UUID targetId, UUID companyId);

    boolean existsByInspection_IdAndCompany_IdAndIsRespondedFalse(UUID inspectionId, UUID companyId);

    @Query("""
            select target
            from InspectionTarget target
            join fetch target.inspection inspection
            join fetch target.member member
            join fetch member.company
            where inspection.id in :inspectionIds
              and target.member is not null
              and member.deletedAt is null
            """)
    List<InspectionTarget> findAllNotificationTargetsByInspectionIdIn(Collection<UUID> inspectionIds);

    @Query("""
            select target
            from InspectionTarget target
            join fetch target.inspection inspection
            join fetch target.member member
            join fetch member.company
            where inspection.inspectionStatus = :inspectionStatus
              and inspection.endDate >= :endDateStartInclusive
              and inspection.endDate < :endDateEndExclusive
              and target.isResponded = false
              and target.member is not null
              and member.deletedAt is null
            """)
    List<InspectionTarget> findAllUnrespondedTargetsForEndingReminder(
            InspectionStatus inspectionStatus,
            LocalDateTime endDateStartInclusive,
            LocalDateTime endDateEndExclusive
    );

}
