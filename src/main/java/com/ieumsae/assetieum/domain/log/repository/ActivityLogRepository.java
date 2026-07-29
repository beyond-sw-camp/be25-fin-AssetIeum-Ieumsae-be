package com.ieumsae.assetieum.domain.log.repository;

import com.ieumsae.assetieum.domain.log.entity.ActivityLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>, JpaSpecificationExecutor<ActivityLog> {

	boolean existsByEventId(UUID eventId);
}
