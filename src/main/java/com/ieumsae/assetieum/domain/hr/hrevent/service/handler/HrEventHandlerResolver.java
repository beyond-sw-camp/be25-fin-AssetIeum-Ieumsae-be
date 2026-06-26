package com.ieumsae.assetieum.domain.hr.hrevent.service.handler;

import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HrEventHandlerResolver {

    private final Map<HrEventType, HrEventHandler> handlers;

    public HrEventHandlerResolver(List<HrEventHandler> handlers) {
        this.handlers = new EnumMap<>(HrEventType.class);
        handlers.forEach(handler -> this.handlers.put(handler.supports(), handler));
    }

    public HrEventHandler resolve(HrEventType eventType) {
        HrEventHandler handler = handlers.get(eventType);
        if (handler == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return handler;
    }
}
