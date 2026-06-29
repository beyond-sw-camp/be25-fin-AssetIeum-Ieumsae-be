package com.ieumsae.assetieum.domain.notification.repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class NotificationEmitterRepository {

	private final ConcurrentMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

	public void save(UUID receiverId, SseEmitter emitter) {
		emitters.computeIfAbsent(receiverId, key -> new CopyOnWriteArrayList<>()).add(emitter);
	}

	public List<SseEmitter> findAllByReceiverId(UUID receiverId) {
		return emitters.getOrDefault(receiverId, new CopyOnWriteArrayList<>());
	}

	public void delete(UUID receiverId, SseEmitter emitter) {
		emitters.computeIfPresent(receiverId, (key, receiverEmitters) -> {
			receiverEmitters.remove(emitter);
			return receiverEmitters.isEmpty() ? null : receiverEmitters;
		});
	}

	public void forEach(BiConsumer<UUID, SseEmitter> consumer) {
		emitters.forEach((receiverId, receiverEmitters) ->
			receiverEmitters.forEach(emitter -> consumer.accept(receiverId, emitter))
		);
	}
}
