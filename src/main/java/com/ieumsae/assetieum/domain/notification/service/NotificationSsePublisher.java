package com.ieumsae.assetieum.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.notification.dto.NotificationListItemResponse;
import com.ieumsae.assetieum.domain.notification.dto.NotificationSseMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSsePublisher {

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final ObjectProvider<ChannelTopic> notificationSseChannelTopic;
	private final NotificationSseService notificationSseService;

	public void publish(UUID receiverId, NotificationListItemResponse response) {
		ChannelTopic channelTopic = notificationSseChannelTopic.getIfAvailable();
		if (channelTopic == null) {
			notificationSseService.sendLocal(receiverId, response);
			return;
		}

		try {
			String payload = objectMapper.writeValueAsString(NotificationSseMessage.from(receiverId, response));
			redisTemplate.convertAndSend(channelTopic.getTopic(), payload);
		} catch (JsonProcessingException exception) {
			log.warn("Failed to serialize notification SSE message. receiverId={}", receiverId, exception);
			notificationSseService.sendLocal(receiverId, response);
		} catch (RuntimeException exception) {
			log.warn("Failed to publish notification SSE message. receiverId={}", receiverId, exception);
			notificationSseService.sendLocal(receiverId, response);
		}
	}
}
