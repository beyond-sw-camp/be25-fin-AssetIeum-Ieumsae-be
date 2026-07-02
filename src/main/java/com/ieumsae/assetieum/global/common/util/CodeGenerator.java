package com.ieumsae.assetieum.global.common.util;

import com.ieumsae.assetieum.global.common.util.KstDateTime;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Duration SEQUENCE_TTL = Duration.ofDays(2);

    private final StringRedisTemplate redisTemplate;

    public String generate(String prefix, String redisKeyPrefix, UUID companyId) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("prefix must not be blank");
        }
        if (redisKeyPrefix == null || redisKeyPrefix.isBlank()) {
            throw new IllegalArgumentException("redisKeyPrefix must not be blank");
        }
        if (companyId == null) {
            throw new IllegalArgumentException("companyId must not be null");
        }

        String date = KstDateTime.today().format(DATE_FORMATTER);
        String redisKey = redisKeyPrefix + companyId + ":" + date;
        Long sequence = redisTemplate.opsForValue().increment(redisKey);

        if (sequence == null) {
            throw new IllegalStateException("Failed to generate code sequence.");
        }

        if (sequence == 1L) {
            redisTemplate.expire(redisKey, SEQUENCE_TTL);
        }

        return prefix.toUpperCase() + "-" + date + "-" + String.format("%05d", sequence);
    }
}
