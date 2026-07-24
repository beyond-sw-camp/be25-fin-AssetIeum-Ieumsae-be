package com.ieumsae.assetieum.domain.notification.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
@Table(name = "notifications")
public class Notification {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Long id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "event_id", unique = true, columnDefinition = "CHAR(36)")
	private UUID eventId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "receiver_id", nullable = false)
	private Member receiver;

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type", nullable = false, length = 50)
	private NotificationType notificationType;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", nullable = false, length = 50)
	private NotificationTargetType targetType;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "target_id", nullable = false, columnDefinition = "CHAR(36)")
	private UUID targetId;

	@Column(name = "is_read", nullable = false)
	private boolean isRead;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now(SEOUL_ZONE);
	}

	public static Notification create(
		UUID eventId,
		Company company,
		Member receiver,
		NotificationType notificationType,
		String title,
		String content,
		NotificationTargetType targetType,
		UUID targetId
	) {
		return Notification.builder()
			.eventId(eventId)
			.company(company)
			.receiver(receiver)
			.notificationType(notificationType)
			.title(title)
			.content(content)
			.targetType(targetType)
			.targetId(targetId)
			.isRead(false)
			.build();
	}

	public static Notification create(
		Company company,
		Member receiver,
		NotificationType notificationType,
		String title,
		String content,
		NotificationTargetType targetType,
		UUID targetId
	) {
		return create(null, company, receiver, notificationType, title, content, targetType, targetId);
	}

	public void markAsRead() {
		this.isRead = true;
	}
}
