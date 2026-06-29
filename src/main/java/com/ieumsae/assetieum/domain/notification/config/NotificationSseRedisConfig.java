package com.ieumsae.assetieum.domain.notification.config;

import com.ieumsae.assetieum.domain.notification.service.NotificationSseRedisSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@ConditionalOnProperty(prefix = "app.notification.sse", name = "redis-enabled", havingValue = "true", matchIfMissing = true)
public class NotificationSseRedisConfig {

	public static final String CHANNEL_NAME = "assetieum:notification:sse";

	@Bean
	public ChannelTopic notificationSseChannelTopic() {
		return new ChannelTopic(CHANNEL_NAME);
	}

	@Bean
	public RedisMessageListenerContainer notificationSseRedisMessageListenerContainer(
		RedisConnectionFactory redisConnectionFactory,
		NotificationSseRedisSubscriber notificationSseRedisSubscriber,
		ChannelTopic notificationSseChannelTopic
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener(notificationSseRedisSubscriber, notificationSseChannelTopic);
		return container;
	}
}
