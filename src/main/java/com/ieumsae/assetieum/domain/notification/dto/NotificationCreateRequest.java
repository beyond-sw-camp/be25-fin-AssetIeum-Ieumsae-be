package com.ieumsae.assetieum.domain.notification.dto;

import com.ieumsae.assetieum.domain.notification.type.NotificationTargetType;
import com.ieumsae.assetieum.domain.notification.type.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationCreateRequest {

	@NotNull(message = "수신자 ID는 필수입니다.")
	private UUID receiverId;

	@NotNull(message = "알림 유형은 필수입니다.")
	private NotificationType notificationType;

	@NotBlank(message = "알림 제목은 필수입니다.")
	@Size(max = 200, message = "알림 제목은 200자 이하여야 합니다.")
	private String title;

	@NotBlank(message = "알림 내용은 필수입니다.")
	private String content;

	@NotNull(message = "알림 대상 유형은 필수입니다.")
	private NotificationTargetType targetType;

	@NotNull(message = "알림 대상 ID는 필수입니다.")
	private UUID targetId;
}
