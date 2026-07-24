package com.ieumsae.assetieum.domain.notification.repository;

import com.ieumsae.assetieum.domain.notification.entity.Notification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	boolean existsByEventId(UUID eventId);

	Page<Notification> findAllByReceiver_IdAndCompany_Id(UUID receiverId, UUID companyId, Pageable pageable);

	Optional<Notification> findByIdAndReceiver_IdAndCompany_Id(Long notificationId, UUID receiverId, UUID companyId);

	long countByReceiver_IdAndCompany_IdAndIsReadFalse(UUID receiverId, UUID companyId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		UPDATE Notification n
		SET n.isRead = true
		WHERE n.receiver.id = :receiverId
			AND n.company.id = :companyId
			AND n.isRead = false
		""")
	int markAllAsRead(
		@Param("receiverId") UUID receiverId,
		@Param("companyId") UUID companyId
	);
}
