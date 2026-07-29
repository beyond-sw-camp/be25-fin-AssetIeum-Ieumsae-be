package com.ieumsae.assetieum.global.kafka.outbox;

import com.ieumsae.assetieum.global.common.util.KstDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "outbox_events")
public class OutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "outbox_event_id")
	private Long id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "event_id", nullable = false, unique = true, columnDefinition = "CHAR(36)")
	private UUID eventId;

	@Column(nullable = false)
	private String topic;

	@Column(name = "event_key", nullable = false)
	private String eventKey;

	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;

	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OutboxStatus status;

	@Column(name = "retry_count", nullable = false)
	private int retryCount;

	@Column(name = "next_retry_at")
	private LocalDateTime nextRetryAt;

	@Column(name = "last_error", length = 1000)
	private String lastError;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	@PrePersist
	void onCreate() {
		createdAt = KstDateTime.now();
		status = OutboxStatus.PENDING;
	}

	public void markPublished() {
		status = OutboxStatus.PUBLISHED;
		publishedAt = KstDateTime.now();
		lastError = null;
	}

	public void markFailed(String error) {
		retryCount++;
		long delaySeconds = Math.min(300L, 1L << Math.min(retryCount, 8));
		nextRetryAt = KstDateTime.now().plusSeconds(delaySeconds);
		lastError = error == null ? "Unknown Kafka publish error" : error.substring(0, Math.min(error.length(), 1000));
	}
}
