package com.ieumsae.assetieum.global.kafka.smoke;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(prefix = "app.kafka.smoke-test", name = "enabled", havingValue = "true")
public class KafkaSmokeTestConfiguration {

	@Bean
	NewTopic kafkaSmokeTestTopic(@Value("${app.kafka.topics.smoke-test}") String topic) {
		return TopicBuilder.name(topic)
			.partitions(1)
			.replicas(1)
			.build();
	}
}
