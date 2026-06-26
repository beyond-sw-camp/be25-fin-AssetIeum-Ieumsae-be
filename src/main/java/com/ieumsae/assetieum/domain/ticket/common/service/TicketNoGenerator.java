package com.ieumsae.assetieum.domain.ticket.common.service;

import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketNoGenerator {

	private static final String TICKET_NO_PREFIX = "TKT";
	private static final String REDIS_KEY_PREFIX = "ticket:no:";

	private final CodeGenerator codeGenerator;

	public String generate(UUID companyId) {
		return codeGenerator.generate(TICKET_NO_PREFIX, REDIS_KEY_PREFIX, companyId);
	}
}
