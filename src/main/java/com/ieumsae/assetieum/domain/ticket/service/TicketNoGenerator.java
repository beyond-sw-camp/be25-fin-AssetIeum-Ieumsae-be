package com.ieumsae.assetieum.domain.ticket.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketNoGenerator {

	private static final DateTimeFormatter TICKET_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final String TICKET_NO_PREFIX = "TKT-";
	private static final String REDIS_KEY_PREFIX = "ticket:no:";
	private static final Duration TICKET_NO_SEQUENCE_TTL = Duration.ofDays(2);

	private final StringRedisTemplate redisTemplate;

	public String generate() {
		String date = LocalDate.now().format(TICKET_DATE_FORMAT);
		Long sequence = redisTemplate.opsForValue().increment(REDIS_KEY_PREFIX + date);

		if (sequence == null) {
			throw new IllegalStateException("Failed to generate ticket number sequence.");
		}

		if (sequence == 1L) {
			redisTemplate.expire(REDIS_KEY_PREFIX + date, TICKET_NO_SEQUENCE_TTL);
		}

		return TICKET_NO_PREFIX + date + "-" + String.format("%03d", sequence);
	}
}
