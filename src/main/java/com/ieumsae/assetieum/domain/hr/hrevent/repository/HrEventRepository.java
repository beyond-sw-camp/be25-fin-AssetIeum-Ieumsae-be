package com.ieumsae.assetieum.domain.hr.hrevent.repository;

import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventStatus;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HrEventRepository extends JpaRepository<HrEvent, UUID>, HrEventRepositoryCustom {
    Optional<HrEvent> findByIdAndCompany_IdAndCancelledAtIsNull(UUID eventId, UUID uuid);

    List<HrEvent> findAllByCompany_IdAndMember_IdAndEventTypeAndHrEventStatusInAndCancelledAtIsNullOrderByEventDateDesc(
            UUID companyId,
            UUID memberId,
            HrEventType eventType,
            List<HrEventStatus> statuses
    );

    List<HrEvent> findAllByHrEventStatusAndEventDateGreaterThanEqualAndEventDateLessThanAndCancelledAtIsNullOrderByEventDateAsc(
            HrEventStatus status,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    List<HrEvent> findAllByHrEventStatusAndEventTypeAndEventDateGreaterThanEqualAndEventDateLessThanAndCancelledAtIsNullOrderByEventDateAsc(
            HrEventStatus status,
            HrEventType eventType,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}
