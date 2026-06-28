package com.ieumsae.assetieum.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.notification.dto.NotificationSseMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.notification.sse", name = "redis-enabled", havingValue = "true", matchIfMissing = true)
public class NotificationSseRedisSubscriber implements MessageListener {

	private final ObjectMapper objectMapper;
	private final NotificationSseService notificationSseService;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String payload = new String(message.getBody(), StandardCharsets.UTF_8);
		try {
			NotificationSseMessage sseMessage = objectMapper.readValue(payload, NotificationSseMessage.class);
			notificationSseService.sendLocal(sseMessage.receiverId(), sseMessage.toResponse());
		} catch (JsonProcessingException exception) {
			log.warn("Failed to deserialize notification SSE message. payload={}", payload, exception);
		}
	}
}
