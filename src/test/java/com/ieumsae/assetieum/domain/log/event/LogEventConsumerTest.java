package com.ieumsae.assetieum.domain.log.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.domain.log.service.LogService;
import com.ieumsae.assetieum.domain.log.type.ActivityLogAction;
import com.ieumsae.assetieum.domain.log.type.LogSubjectType;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LogEventConsumerTest {

	@Test
	void convertsAndPersistsActivityLogEvent() {
		ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		LogService logService = mock(LogService.class);
		LogEventConsumer consumer = new LogEventConsumer(objectMapper, logService);
		ActivityLogEvent payload = new ActivityLogEvent(
			UUID.randomUUID(),
			ActivityLogAction.VIEW,
			LogSubjectType.TICKET,
			UUID.randomUUID(),
			"/api/v1/tickets"
		);
		EventEnvelope<ActivityLogEvent> event = new EventEnvelope<>(
			UUID.randomUUID(),
			"ACTIVITY_LOG_CREATED",
			Instant.now(),
			UUID.randomUUID(),
			payload
		);
		JsonNode message = objectMapper.valueToTree(event);

		consumer.consumeActivityLog(message);

		verify(logService).persistActivityLogEvent(event.eventId(), event.companyId(), payload);
	}
}
