package com.ieumsae.assetieum.global.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class KstDateTime {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	private KstDateTime() {
	}

	public static LocalDateTime now() {
		return LocalDateTime.now(SEOUL_ZONE);
	}

	public static LocalDate today() {
		return LocalDate.now(SEOUL_ZONE);
	}
}
