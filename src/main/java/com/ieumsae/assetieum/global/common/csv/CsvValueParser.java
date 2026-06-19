package com.ieumsae.assetieum.global.common.csv;

import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class CsvValueParser {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy. M. d"),
            DateTimeFormatter.ofPattern("yyyy.M.d"),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    private CsvValueParser() {
    }

    public static String parseNullableString(String value) {
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    public static Boolean parseBoolean(String value) {
        String trimmedValue = value.trim();
        if ("true".equalsIgnoreCase(trimmedValue)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmedValue)) {
            return false;
        }
        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    public static <T extends Enum<T>> T parseEnum(Class<T> enumType, String value) {
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public static <T extends Enum<T>> T parseNullableEnum(Class<T> enumType, String value) {
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return null;
        }

        return parseEnum(enumType, trimmedValue);
    }

    public static Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public static BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value.trim());
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public static LocalDateTime parseDateTime(String value) {
        String trimmedValue = value.trim();

        try {
            return LocalDateTime.parse(trimmedValue);
        } catch (DateTimeParseException ignored) {
            // Try date-only CSV formats below.
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmedValue, formatter).atStartOfDay();
            } catch (DateTimeParseException ignored) {
                // Try the next supported CSV date format.
            }
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }
}
