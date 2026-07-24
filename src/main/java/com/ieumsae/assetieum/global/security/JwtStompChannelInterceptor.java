package com.ieumsae.assetieum.global.security;

import com.ieumsae.assetieum.domain.ticket.common.repository.TicketRepository;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String COMMENT_TOPIC_PREFIX = "/topic/tickets/";
	private static final String COMMENT_TOPIC_SUFFIX = "/comments";

	private final JwtProvider jwtProvider;
	private final TokenRedisService tokenRedisService;
	private final TicketRepository ticketRepository;

	@Override
	public Message<?> preSend(
		Message<?> message,
		MessageChannel channel
	) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
			message,
			StompHeaderAccessor.class
		);

		if (accessor == null) {
			return message;
		}

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			authenticate(accessor);
		}

		if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
			authorizeSubscription(accessor);
		}

		return message;
	}

	private void authenticate(StompHeaderAccessor accessor) {
		String authorization = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}

		String token = authorization.substring(BEARER_PREFIX.length());

		if (tokenRedisService.isAccessTokenBlacklisted(token)) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}

		AuthenticatedMember authenticatedMember = jwtProvider.parseAccessToken(token);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			authenticatedMember,
			null,
			List.of(new SimpleGrantedAuthority("ROLE_" + authenticatedMember.role().name()))
		);

		accessor.setUser(authentication);
	}

	private void authorizeSubscription(StompHeaderAccessor accessor) {
		String destination = accessor.getDestination();

		if (!isCommentTopic(destination)) {
			return;
		}

		AuthenticatedMember authenticatedMember = getAuthenticatedMember(accessor.getUser());
		UUID ticketId = parseTicketId(destination);

		boolean exists = ticketRepository.existsByIdAndCompany_IdAndDeletedAtIsNull(
			ticketId,
			authenticatedMember.companyId()
		);

		if (!exists) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private boolean isCommentTopic(String destination) {
		return destination != null
			&& destination.startsWith(COMMENT_TOPIC_PREFIX)
			&& destination.endsWith(COMMENT_TOPIC_SUFFIX);
	}

	private UUID parseTicketId(String destination) {
		String ticketId = destination.substring(
			COMMENT_TOPIC_PREFIX.length(),
			destination.length() - COMMENT_TOPIC_SUFFIX.length()
		);

		try {
			return UUID.fromString(ticketId);
		} catch (IllegalArgumentException exception) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private AuthenticatedMember getAuthenticatedMember(Principal principal) {
		if (!(principal instanceof UsernamePasswordAuthenticationToken authentication)
			|| !(authentication.getPrincipal() instanceof AuthenticatedMember authenticatedMember)) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}

		return authenticatedMember;
	}
}
