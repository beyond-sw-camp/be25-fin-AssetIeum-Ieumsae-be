package com.ieumsae.assetieum.global.kafka.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieumsae.assetieum.global.kafka.event.EventEnvelope;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OutboxServiceTest {

	private final OutboxEventRepository repository = mock(OutboxEventRepository.class);
	private final OutboxService service = new OutboxService(repository, new ObjectMapper().findAndRegisterModules());

	@Test
	void storesEventEnvelopeInsteadOfPublishingImmediately() {
		EventEnvelope<Map<String, String>> event = EventEnvelope.of(
			"TEST_EVENT", UUID.randomUUID(), Map.of("value", "saved")
		);

		service.enqueue("assetieum.test.v1", "member-1", event);

		ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
		verify(repository).save(captor.capture());
		OutboxEvent saved = captor.getValue();
		assertThat(saved.getEventId()).isEqualTo(event.eventId());
		assertThat(saved.getTopic()).isEqualTo("assetieum.test.v1");
		assertThat(saved.getEventKey()).isEqualTo("member-1");
		assertThat(saved.getPayload()).contains(event.eventId().toString(), "saved");
	}
}
