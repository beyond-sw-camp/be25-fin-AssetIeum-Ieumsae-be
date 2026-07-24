package com.ieumsae.assetieum.domain.log.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnExpression("${app.kafka.log.enabled:false} or ${app.kafka.notification.enabled:false}")
public class KafkaLogConfiguration {

	private static final int PARTITION_COUNT = 3;
	private static final short REPLICA_COUNT = 1;
	private static final String DEAD_LETTER_SUFFIX = ".DLT";

	@Bean
	KafkaAdmin.NewTopics logTopics(
		@Value("${app.kafka.topics.activity-log}") String activityLogTopic,
		@Value("${app.kafka.topics.audit-log}") String auditLogTopic,
		@Value("${app.kafka.topics.notification}") String notificationTopic
	) {
		return new KafkaAdmin.NewTopics(
			createTopic(activityLogTopic),
			createTopic(auditLogTopic),
			createTopic(notificationTopic),
			createTopic(activityLogTopic + DEAD_LETTER_SUFFIX),
			createTopic(auditLogTopic + DEAD_LETTER_SUFFIX),
			createTopic(notificationTopic + DEAD_LETTER_SUFFIX)
		);
	}

	@Bean
	CommonErrorHandler kafkaLogErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
			kafkaTemplate,
			(record, exception) -> new TopicPartition(record.topic() + DEAD_LETTER_SUFFIX, record.partition())
		);
		return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
	}

	private NewTopic createTopic(String name) {
		return TopicBuilder.name(name)
			.partitions(PARTITION_COUNT)
			.replicas(REPLICA_COUNT)
			.build();
	}
}
