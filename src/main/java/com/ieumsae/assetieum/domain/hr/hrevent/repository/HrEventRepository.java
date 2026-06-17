package com.ieumsae.assetieum.domain.hr.hrevent.repository;

import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HrEventRepository extends JpaRepository<HrEvent, UUID>, HrEventRepositoryCustom {
    Optional<HrEvent> findByIdAndCompany_IdAndCancelledAtIsNull(UUID eventId, UUID uuid);
}
