package com.ieumsae.assetieum.global.kafka.outbox;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

	@Query("""
		select e from OutboxEvent e
		where e.status = com.ieumsae.assetieum.global.kafka.outbox.OutboxStatus.PENDING
		  and (e.nextRetryAt is null or e.nextRetryAt <= :now)
		order by e.createdAt
		""")
	List<OutboxEvent> findPublishable(@Param("now") LocalDateTime now, Pageable pageable);
}
